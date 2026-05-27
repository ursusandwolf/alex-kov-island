package com.island.engine.core;

import com.island.engine.ecs.ComponentRegistry;
import com.island.engine.ecs.EntityQuery;
import com.island.engine.event.EventBus;
import com.island.engine.model.Mortal;
import com.island.engine.model.Tickable;
import com.island.engine.model.WorldSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GridSpatialIndexTest {

    private SimulationWorld<TestEntity> world;
    private GridSpatialIndex<TestEntity> spatialIndex;
    private TestNode[][] grid;

    @BeforeEach
    void setUp() {
        world = mock(SimulationWorld.class);
        grid = new TestNode[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                grid[x][y] = new TestNode(x, y, world);
            }
        }

        when(world.getNode(any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            TestNode current = invocation.getArgument(0);
            int dx = invocation.getArgument(1);
            int dy = invocation.getArgument(2);
            int tx = current.x + dx;
            int ty = current.y + dy;
            if (tx >= 0 && tx < 5 && ty >= 0 && ty < 5) {
                return Optional.of(grid[tx][ty]);
            }
            return Optional.empty();
        });

        spatialIndex = new GridSpatialIndex<>(world);
    }

    @Test
    void shouldFindInRadius0() {
        TestEntity e1 = new TestEntity();
        grid[2][2].addEntity(e1);

        List<TestEntity> found = spatialIndex.getInRadius(grid[2][2], 0);
        assertEquals(1, found.size());
        assertTrue(found.contains(e1));
    }

    @Test
    void shouldFindInRadius1() {
        TestEntity eCenter = new TestEntity();
        TestEntity eNeighbor = new TestEntity();
        TestEntity eFar = new TestEntity();

        grid[2][2].addEntity(eCenter);
        grid[2][3].addEntity(eNeighbor);
        grid[4][4].addEntity(eFar);

        List<TestEntity> found = spatialIndex.getInRadius(grid[2][2], 1);
        assertEquals(2, found.size());
        assertTrue(found.contains(eCenter));
        assertTrue(found.contains(eNeighbor));
        assertFalse(found.contains(eFar));
    }

    @Test
    void shouldFindNearest() {
        TestEntity eClose = new TestEntity();
        TestEntity eFurther = new TestEntity();

        grid[2][3].addEntity(eClose);
        grid[2][4].addEntity(eFurther);

        TestEntity nearest = spatialIndex.getNearest(grid[2][2], 5, null);
        assertEquals(eClose, nearest);
    }

    private static class TestEntity implements Mortal {
        private boolean alive = true;
        @Override public boolean isAlive() { return alive; }
        @Override public void die() { alive = false; }
        @Override public String getTypeName() { return "test"; }
    }

    private static class TestNode implements SimulationNode<TestEntity> {
        final int x, y;
        final SimulationWorld<TestEntity> world;
        final List<TestEntity> entities = new ArrayList<>();
        final Lock lock = new ReentrantLock();

        TestNode(int x, int y, SimulationWorld<TestEntity> world) {
            this.x = x; this.y = y; this.world = world;
        }

        @Override public Lock getLock() { return lock; }
        @Override public String getCoordinates() { return x + "," + y; }
        @Override public void setNeighbors(List<SimulationNode<TestEntity>> neighbors) {}
        @Override public List<SimulationNode<TestEntity>> getNeighbors() { return null; }
        @Override public SimulationWorld<TestEntity> getWorld() { return world; }
        @Override public List<TestEntity> getEntities() { return entities; }
        @Override public void forEachEntity(Consumer<TestEntity> action) { entities.forEach(action); }
        @Override public int getEntityCount() { return entities.size(); }
        @Override public boolean canAccept(TestEntity entity) { return true; }
        @Override public boolean addEntity(TestEntity entity) { return entities.add(entity); }
        @Override public boolean removeEntity(TestEntity entity) { return entities.remove(entity); }
        @Override public void cleanupDeadEntities(Consumer<TestEntity> onEntityRemoved) {}
    }
}
