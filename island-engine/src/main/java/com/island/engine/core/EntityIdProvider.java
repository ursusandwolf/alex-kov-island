package com.island.engine.core;

import com.island.engine.internal.EntityIdManager;

/**
 * Public API for managing unique entity IDs.
 */
@EngineAPI
public interface EntityIdProvider {
    int acquireId();
    void releaseId(int id);

    static EntityIdProvider create() {
        return new EntityIdManager();
    }
}
