package com.island.engine.core;

import com.island.engine.ecs.EntityQuery;
import com.island.engine.model.Mortal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of SpatialIndex optimized for grid-based worlds.
 * Uses the existing grid structure of SimulationWorld.
 */
@InternalEngine
public class GridSpatialIndex<T extends Mortal> implements SpatialIndex<T> {
    private final SimulationWorld<T> world;

    public GridSpatialIndex(SimulationWorld<T> world) {
        this.world = world;
    }

    @Override
    public List<T> getInRadius(SimulationNode<T> center, int radius) {
        List<T> result = new ArrayList<>();
        queryInRadius(center, radius, null, result::add);
        return result;
    }

    @Override
    public void queryInRadius(SimulationNode<T> center, int radius, EntityQuery<T> query, Consumer<T> action) {
        if (radius < 0) return;
        
        // Optimize for radius 0
        if (radius == 0) {
            if (query == null) {
                center.forEachEntity(action);
            } else {
                center.query(query, action);
            }
            return;
        }

        // Search in a square box around the center
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                // Manhattan distance check (optional, but circle search is usually better for radius > 1)
                // For a grid simulation, a square search is often what's expected.
                // If we want a circle, we can use: if (dx*dx + dy*dy > radius*radius) continue;
                
                world.getNode(center, dx, dy).ifPresent(node -> {
                    if (query == null) {
                        node.forEachEntity(action);
                    } else {
                        node.query(query, action);
                    }
                });
            }
        }
    }

    @Override
    public T getNearest(SimulationNode<T> center, int radius, EntityQuery<T> query) {
        // Spiral search or expanding boxes to find the nearest
        for (int r = 0; r <= radius; r++) {
            final T[] found = (T[]) new Mortal[1];
            
            // Search at exact radius r
            searchAtRadius(center, r, query, entity -> {
                if (found[0] == null) {
                    found[0] = entity;
                }
            });
            
            if (found[0] != null) {
                return found[0];
            }
        }
        return null;
    }

    private void searchAtRadius(SimulationNode<T> center, int r, EntityQuery<T> query, Consumer<T> action) {
        if (r == 0) {
            if (query == null) center.forEachEntity(action);
            else center.query(query, action);
            return;
        }

        // Top and bottom edges
        for (int dx = -r; dx <= r; dx++) {
            processNode(center, dx, -r, query, action);
            processNode(center, dx, r, query, action);
        }
        // Left and right edges (excluding corners already covered)
        for (int dy = -r + 1; dy <= r - 1; dy++) {
            processNode(center, -r, dy, query, action);
            processNode(center, r, dy, query, action);
        }
    }

    private void processNode(SimulationNode<T> center, int dx, int dy, EntityQuery<T> query, Consumer<T> action) {
        world.getNode(center, dx, dy).ifPresent(node -> {
            if (query == null) node.forEachEntity(action);
            else node.query(query, action);
        });
    }
}
