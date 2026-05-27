package com.island.engine.ecs;

import com.island.engine.core.EngineAPI;
import com.island.engine.internal.ArrayComponentStore;
import com.island.engine.internal.DefaultComponentStore;
import java.util.BitSet;

/**
 * Interface for typed component storage.
 */
@EngineAPI
public interface ComponentStore {
    /**
     * Creates a default map-based component store.
     */
    static ComponentStore createDefault(ComponentRegistry registry) {
        return new DefaultComponentStore(registry);
    }

    /**
     * Creates a high-performance array-based component store.
     */
    static ComponentStore createArray(ComponentRegistry registry) {
        return new ArrayComponentStore(registry);
    }

    <C extends Component> void add(C component);

    <C extends Component> C get(Class<C> type);

    <C extends Component> boolean has(Class<C> type);

    <C extends Component> void remove(Class<C> type);

    BitSet getComponentBitSet();
}
