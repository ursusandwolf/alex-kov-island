package com.island.engine.core;

import com.island.engine.ecs.EntityQuery;
import com.island.engine.model.Mortal;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for spatial indexing of entities to enable efficient proximity queries.
 * 
 * @param <T> The base type of entities.
 */
@EngineAPI
public interface SpatialIndex<T extends Mortal> {
    /**
     * Finds all entities within a specific radius of a center node.
     * 
     * @param center the starting node for the search.
     * @param radius the search radius in grid units.
     * @return a list of entities within the specified range.
     */
    List<T> getInRadius(SimulationNode<T> center, int radius);

    /**
     * Executes an action for every entity within a specific radius that matches a query.
     * 
     * @param center the starting node for the search.
     * @param radius the search radius in grid units.
     * @param query the component filter.
     * @param action the callback for matching entities.
     */
    void queryInRadius(SimulationNode<T> center, int radius, EntityQuery<T> query, Consumer<T> action);

    /**
     * Returns the nearest entity matching the query within the radius.
     * 
     * @param center the starting node.
     * @param radius search limit.
     * @param query filter.
     * @return the closest matching entity, if any.
     */
    T getNearest(SimulationNode<T> center, int radius, EntityQuery<T> query);
}
