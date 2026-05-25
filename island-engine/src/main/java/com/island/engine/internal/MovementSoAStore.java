package com.island.engine.internal;

import com.island.engine.core.MovementStorage;
import com.island.engine.core.InternalEngine;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.StampedLock;

/**
 * High-performance, primitive-based store for movement SoA components.
 * Uses AtomicIntegerArray for thread-safe element access and StampedLock for safe resizing.
 * 
 * <p><b>Important:</b> StampedLock is NOT reentrant. Methods in this class must not be 
 * called recursively or from contexts that already hold a write lock on this instance 
 * to avoid deadlocks.
 */
@InternalEngine
public final class MovementSoAStore implements MovementStorage {
    private volatile AtomicIntegerArray speeds;
    private volatile AtomicIntegerArray ranges;
    private volatile int capacity;
    private final StampedLock lock = new StampedLock();

    public MovementSoAStore(int initialCapacity) {
        this.capacity = initialCapacity;
        this.speeds = new AtomicIntegerArray(initialCapacity);
        this.ranges = new AtomicIntegerArray(initialCapacity);
    }

    @Override
    public void set(int entityId, int speed, int range) {
        long stamp = lock.readLock();
        try {
            if (entityId < capacity) {
                this.speeds.set(entityId, speed);
                this.ranges.set(entityId, range);
                return;
            }
        } finally {
            lock.unlockRead(stamp);
        }

        // Need expansion
        stamp = lock.writeLock();
        try {
            ensureCapacityInternal(entityId);
            this.speeds.set(entityId, speed);
            this.ranges.set(entityId, range);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public int getSpeed(int entityId) {
        long stamp = lock.tryOptimisticRead();
        int cap = capacity;
        AtomicIntegerArray arr = speeds;
        int speed = (entityId < cap) ? arr.get(entityId) : 0;
        
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                cap = capacity;
                arr = speeds;
                speed = (entityId < cap) ? arr.get(entityId) : 0;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return speed;
    }

    @Override
    public int getRange(int entityId) {
        long stamp = lock.tryOptimisticRead();
        int cap = capacity;
        AtomicIntegerArray arr = ranges;
        int range = (entityId < cap) ? arr.get(entityId) : 0;
        
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                cap = capacity;
                arr = ranges;
                range = (entityId < cap) ? arr.get(entityId) : 0;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return range;
    }

    private void ensureCapacityInternal(int entityId) {
        if (entityId >= capacity) {
            int newCapacity = Math.max(entityId + 1, capacity * 2);
            AtomicIntegerArray newSpeeds = new AtomicIntegerArray(newCapacity);
            AtomicIntegerArray newRanges = new AtomicIntegerArray(newCapacity);
            for (int i = 0; i < capacity; i++) {
                newSpeeds.set(i, speeds.get(i));
                newRanges.set(i, ranges.get(i));
            }
            this.speeds = newSpeeds;
            this.ranges = newRanges;
            this.capacity = newCapacity;
        }
    }
}
