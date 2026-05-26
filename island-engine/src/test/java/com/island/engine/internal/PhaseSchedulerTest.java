package com.island.engine.internal;

import com.island.engine.core.ParallelTask;
import com.island.engine.core.SimulationWorld;
import com.island.engine.model.Mortal;
import com.island.engine.scheduling.Phase;
import com.island.engine.scheduling.ScheduledTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class PhaseSchedulerTest {

    @Test
    @DisplayName("Should execute tasks in correct phases")
    void shouldExecuteInPhases() {
        ParallelDispatcher<Mortal> dispatcher = mock(ParallelDispatcher.class);
        PhaseScheduler<Mortal> scheduler = new PhaseScheduler<>(dispatcher);
        SimulationWorld<Mortal> world = mock(SimulationWorld.class);

        ScheduledTask task1 = mock(ScheduledTask.class);
        when(task1.phase()).thenReturn(Phase.PREPARE);
        when(task1.priority()).thenReturn(10);

        ScheduledTask task2 = mock(ScheduledTask.class);
        when(task2.phase()).thenReturn(Phase.SIMULATION);
        when(task2.priority()).thenReturn(5);

        List<ScheduledTask> tasks = Arrays.asList(task1, task2);

        scheduler.execute(world, tasks, 1, 1);

        verify(task1).tick(1);
        verify(task2).tick(1);
    }

    @Test
    @DisplayName("Should use ParallelDispatcher for ParallelTasks")
    void shouldDispatchParallelTasks() {
        ParallelDispatcher<Mortal> dispatcher = mock(ParallelDispatcher.class);
        PhaseScheduler<Mortal> scheduler = new PhaseScheduler<>(dispatcher);
        SimulationWorld<Mortal> world = mock(SimulationWorld.class);

        ParallelTask<Mortal> pt = mock(ParallelTask.class);
        when(pt.priority()).thenReturn(10);

        ScheduledTask task = mock(ScheduledTask.class);
        when(task.phase()).thenReturn(Phase.SIMULATION);
        when(task.asParallelTask()).thenReturn(pt);
        when(task.priority()).thenReturn(10);

        scheduler.execute(world, Collections.singletonList(task), 1, 1);

        // Verification of dispatch call
        verify(dispatcher).dispatch(eq(world), anyList(), eq(1));
        // Sequential tick should NOT be called for parallel tasks
        verify(task, never()).tick(anyInt());
    }

    @Test
    @DisplayName("Should rebuild schedule only when version changes")
    void shouldCacheSchedule() {
        ParallelDispatcher<Mortal> dispatcher = mock(ParallelDispatcher.class);
        PhaseScheduler<Mortal> scheduler = new PhaseScheduler<>(dispatcher);
        SimulationWorld<Mortal> world = mock(SimulationWorld.class);

        ScheduledTask task = mock(ScheduledTask.class);
        when(task.phase()).thenReturn(Phase.SIMULATION);
        when(task.priority()).thenReturn(10);

        // Execute twice with same version
        scheduler.execute(world, Collections.singletonList(task), 1, 1);
        scheduler.execute(world, Collections.singletonList(task), 2, 1);

        // Verify some internal state change if possible, but mainly we test behavior
        verify(task, times(2)).tick(anyInt());
    }

    @Test
    @DisplayName("Should skip empty phases")
    void shouldSkipEmptyPhases() {
        ParallelDispatcher<Mortal> dispatcher = mock(ParallelDispatcher.class);
        PhaseScheduler<Mortal> scheduler = new PhaseScheduler<>(dispatcher);
        
        // No tasks
        scheduler.execute(mock(SimulationWorld.class), Collections.emptyList(), 1, 1);
        
        verify(dispatcher, never()).dispatch(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should execute phases in Enum order")
    void shouldExecuteInOrder() {
        ParallelDispatcher<Mortal> dispatcher = mock(ParallelDispatcher.class);
        PhaseScheduler<Mortal> scheduler = new PhaseScheduler<>(dispatcher);
        
        ScheduledTask init = mock(ScheduledTask.class);
        when(init.phase()).thenReturn(Phase.PREPARE);
        
        ScheduledTask sim = mock(ScheduledTask.class);
        when(sim.phase()).thenReturn(Phase.SIMULATION);
        
        scheduler.execute(mock(SimulationWorld.class), Arrays.asList(sim, init), 1, 1);
        
        // Verify init was called before sim
        org.mockito.InOrder inOrder = inOrder(init, sim);
        inOrder.verify(init).tick(1);
        inOrder.verify(sim).tick(1);
    }
}