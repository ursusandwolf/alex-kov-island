package com.island.engine.internal;

import com.island.engine.core.ParallelTask;
import com.island.engine.ecs.Component;
import com.island.engine.ecs.EntitySystem;
import com.island.engine.model.Mortal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemExecutionGraphTest {

    static class TestComponentA implements Component {}
    static class TestComponentB implements Component {}

    static class TestSystem implements EntitySystem<Mortal> {
        private final int priority;
        private final List<Class<? extends Component>> reads;
        private final List<Class<? extends Component>> writes;

        TestSystem(int priority, List<Class<? extends Component>> reads, List<Class<? extends Component>> writes) {
            this.priority = priority;
            this.reads = reads;
            this.writes = writes;
        }

        @Override public int priority() { return priority; }
        @Override public List<Class<? extends Component>> readComponents() { return reads; }
        @Override public List<Class<? extends Component>> writeComponents() { return writes; }
        @Override public void processCell(com.island.engine.core.SimulationNode<Mortal> node, int tickCount) {}
    }

    @Test
    @DisplayName("Should group independent systems together")
    void shouldGroupIndependent() {
        ParallelTask<Mortal> task1 = new TestSystem(100, List.of(TestComponentA.class), List.of());
        ParallelTask<Mortal> task2 = new TestSystem(90, List.of(TestComponentB.class), List.of());

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(task1, task2));

        assertEquals(1, schedule.size(), "Should be 1 batch");
        assertEquals(2, schedule.get(0).size());
    }

    @Test
    @DisplayName("Should split conflicting systems")
    void shouldSplitConflicting() {
        // Task 1 writes to A
        ParallelTask<Mortal> task1 = new TestSystem(100, List.of(), List.of(TestComponentA.class));
        // Task 2 reads from A
        ParallelTask<Mortal> task2 = new TestSystem(90, List.of(TestComponentA.class), List.of());

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(task1, task2));

        assertEquals(2, schedule.size(), "Should be 2 batches");
        assertEquals(task1, schedule.get(0).get(0));
        assertEquals(task2, schedule.get(1).get(0));
    }

    @Test
    @DisplayName("Should respect priority even when splitting")
    void shouldRespectPriority() {
        ParallelTask<Mortal> high = new TestSystem(1000, List.of(), List.of(TestComponentA.class));
        ParallelTask<Mortal> low = new TestSystem(1, List.of(TestComponentA.class), List.of());

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(low, high));

        assertEquals(2, schedule.size());
        assertEquals(high, schedule.get(0).get(0));
        assertEquals(low, schedule.get(1).get(0));
    }

    @Test
    @DisplayName("Should handle empty tasks")
    void shouldHandleEmpty() {
        assertTrue(SystemExecutionGraph.buildSchedule(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("Should detect write-write conflicts")
    void shouldDetectWriteWriteConflicts() {
        ParallelTask<Mortal> task1 = new TestSystem(10, List.of(), List.of(TestComponentA.class));
        ParallelTask<Mortal> task2 = new TestSystem(5, List.of(), List.of(TestComponentA.class));

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(task1, task2));

        assertEquals(2, schedule.size());
    }

    @Test
    @DisplayName("Should group systems with same priority if no conflict")
    void shouldGroupSamePriority() {
        ParallelTask<Mortal> task1 = new TestSystem(100, List.of(TestComponentA.class), List.of());
        ParallelTask<Mortal> task2 = new TestSystem(100, List.of(TestComponentB.class), List.of());

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(task1, task2));

        assertEquals(1, schedule.size());
        assertEquals(2, schedule.get(0).size());
    }

    @Test
    @DisplayName("Should split systems with same priority if they conflict")
    void shouldSplitSamePriorityConflict() {
        ParallelTask<Mortal> task1 = new TestSystem(100, List.of(), List.of(TestComponentA.class));
        ParallelTask<Mortal> task2 = new TestSystem(100, List.of(TestComponentA.class), List.of());

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(task1, task2));

        assertEquals(2, schedule.size());
    }

    @Test
    @DisplayName("Should handle complex dependency chain")
    void shouldHandleChain() {
        // A -> B -> C
        ParallelTask<Mortal> task1 = new TestSystem(100, List.of(), List.of(TestComponentA.class));
        ParallelTask<Mortal> task2 = new TestSystem(90, List.of(TestComponentA.class), List.of(TestComponentB.class));
        ParallelTask<Mortal> task3 = new TestSystem(80, List.of(TestComponentB.class), List.of());

        List<List<ParallelTask<Mortal>>> schedule = SystemExecutionGraph.buildSchedule(Arrays.asList(task3, task2, task1));

        assertEquals(3, schedule.size());
        assertEquals(task1, schedule.get(0).get(0));
        assertEquals(task2, schedule.get(1).get(0));
        assertEquals(task3, schedule.get(2).get(0));
    }
}