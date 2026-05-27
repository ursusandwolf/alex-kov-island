package com.island.engine.core;

import com.island.engine.internal.MovementSoAStore;

/**
 * Public API for high-performance movement data storage.
 */
@EngineAPI
public interface MovementStorage {
    void set(int entityId, int speed, int range);
    int getSpeed(int entityId);
    int getRange(int entityId);

    static MovementStorage create(int initialCapacity) {
        return new MovementSoAStore(initialCapacity);
    }
}

