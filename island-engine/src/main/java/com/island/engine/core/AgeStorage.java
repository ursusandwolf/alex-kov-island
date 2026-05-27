package com.island.engine.core;

import com.island.engine.internal.AgeSoAStore;

/**
 * Public API for high-performance age data storage.
 */
@EngineAPI
public interface AgeStorage {
    void set(int entityId, int age, int maxLifespan);
    int getAge(int entityId);
    int getMaxLifespan(int entityId);
    void setAge(int entityId, int age);
    void setMaxLifespan(int entityId, int maxLifespan);

    static AgeStorage create(int initialCapacity) {
        return new AgeSoAStore(initialCapacity);
    }
}
