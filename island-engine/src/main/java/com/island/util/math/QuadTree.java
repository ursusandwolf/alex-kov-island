package com.island.util.math;

import com.island.engine.model.Mortal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A simple QuadTree implementation for spatial partitioning of entities with coordinates.
 */
public class QuadTree<T extends Mortal> {
    private static final int MAX_ENTITIES = 10;
    private static final int MAX_LEVELS = 5;

    private int level;
    private List<EntityWrapper<T>> entities;
    private double x, y, width, height;
    private QuadTree<T>[] nodes;

    public QuadTree(int level, double x, double y, double width, double height) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.entities = new ArrayList<>();
        this.nodes = null;
    }

    public void clear() {
        entities.clear();
        if (nodes != null) {
            for (QuadTree<T> node : nodes) {
                node.clear();
            }
            nodes = null;
        }
    }

    private void split() {
        double subWidth = width / 2;
        double subHeight = height / 2;
        nodes = new QuadTree[4];
        nodes[0] = new QuadTree<>(level + 1, x + subWidth, y, subWidth, subHeight);
        nodes[1] = new QuadTree<>(level + 1, x, y, subWidth, subHeight);
        nodes[2] = new QuadTree<>(level + 1, x, y + subHeight, subWidth, subHeight);
        nodes[3] = new QuadTree<>(level + 1, x + subWidth, y + subHeight, subWidth, subHeight);
    }

    private int getIndex(double ex, double ey) {
        int index = -1;
        double verticalMidpoint = x + (width / 2);
        double horizontalMidpoint = y + (height / 2);

        boolean topQuadrant = (ey < horizontalMidpoint);
        boolean bottomQuadrant = (ey >= horizontalMidpoint);

        if (ex < verticalMidpoint) {
            if (topQuadrant) index = 1;
            else if (bottomQuadrant) index = 2;
        } else if (ex >= verticalMidpoint) {
            if (topQuadrant) index = 0;
            else if (bottomQuadrant) index = 3;
        }

        return index;
    }

    public void insert(T entity, double ex, double ey) {
        if (nodes != null) {
            int index = getIndex(ex, ey);
            if (index != -1) {
                nodes[index].insert(entity, ex, ey);
                return;
            }
        }

        entities.add(new EntityWrapper<>(entity, ex, ey));

        if (entities.size() > MAX_ENTITIES && level < MAX_LEVELS) {
            if (nodes == null) {
                split();
            }

            int i = 0;
            while (i < entities.size()) {
                EntityWrapper<T> wrapper = entities.get(i);
                int index = getIndex(wrapper.x, wrapper.y);
                if (index != -1) {
                    nodes[index].insert(wrapper.entity, wrapper.x, wrapper.y);
                    entities.remove(i);
                } else {
                    i++;
                }
            }
        }
    }

    public void query(double qx, double qy, double qradius, Consumer<T> action) {
        if (nodes != null) {
            int index = getIndex(qx, qy); // This is simplified, should check overlaps
            // For simplicity in this example, we check all nodes that intersect the query area
            for (QuadTree<T> node : nodes) {
                if (node.intersects(qx - qradius, qy - qradius, qradius * 2, qradius * 2)) {
                    node.query(qx, qy, qradius, action);
                }
            }
        }

        for (EntityWrapper<T> wrapper : entities) {
            double dx = wrapper.x - qx;
            double dy = wrapper.y - qy;
            if (dx * dx + dy * dy <= qradius * qradius) {
                action.accept(wrapper.entity);
            }
        }
    }

    private boolean intersects(double qx, double qy, double qw, double qh) {
        return !(qx > x + width || qx + qw < x || qy > y + height || qy + qh < y);
    }

    private static class EntityWrapper<T> {
        T entity;
        double x, y;
        EntityWrapper(T entity, double x, double y) {
            this.entity = entity;
            this.x = x;
            this.y = y;
        }
    }
}
