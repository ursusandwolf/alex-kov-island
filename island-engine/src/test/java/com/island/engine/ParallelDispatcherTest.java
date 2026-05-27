package com.island.engine;

import com.island.engine.core.ParallelTask;
import com.island.engine.core.SimulationNode;
import com.island.engine.core.SimulationWorld;
import com.island.engine.core.WorkUnit;
import com.island.engine.internal.ParallelDispatcher;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ParallelDispatcherTest {

    @Test
    @DisplayName("ParallelDispatcher: Lifecycle and execution")
    void dispatcher_test() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ParallelDispatcher dispatcher = new ParallelDispatcher(executor);
        
        SimulationWorld world = mock(SimulationWorld.class);
        WorkUnit unit1 = mock(WorkUnit.class);
        WorkUnit unit2 = mock(WorkUnit.class);
        SimulationNode node1 = mock(SimulationNode.class);
        SimulationNode node2 = mock(SimulationNode.class);

        when(world.getParallelWorkUnits()).thenReturn(Arrays.asList(unit1, unit2));
        when(unit1.iterator()).thenReturn(Collections.singletonList(node1).iterator());
        when(unit2.iterator()).thenReturn(Collections.singletonList(node2).iterator());
        
        ParallelTask service = mock(ParallelTask.class);
        List<ParallelTask> services = Collections.singletonList(service);
        
        dispatcher.dispatch(world, services, 1);
        
        verify(service).beforeTick(1);
        verify(service).processCell(node1, 1);
        verify(service).processCell(node2, 1);
        verify(service).afterTick(1);
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("ParallelDispatcher: Service exception handling")
    void dispatcher_exception_test() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ParallelDispatcher dispatcher = new ParallelDispatcher(executor);
        
        SimulationWorld world = mock(SimulationWorld.class);
        WorkUnit unit = mock(WorkUnit.class);
        when(world.getParallelWorkUnits()).thenReturn(Collections.singletonList(unit));
        when(unit.iterator()).thenReturn(Collections.emptyIterator());
        
        ParallelTask service = mock(ParallelTask.class);
        doThrow(new RuntimeException("Fail")).when(service).beforeTick(anyInt());
        
        dispatcher.dispatch(world, Collections.singletonList(service), 1);
        
        verify(service).beforeTick(1);
        // Dispatcher should continue despite exception in beforeTick
        verify(service).afterTick(1);
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("ParallelDispatcher: Handle null world and shutdown executor")
    void dispatcher_edge_cases() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ParallelDispatcher dispatcher = new ParallelDispatcher(executor);
        
        // Null world
        assertDoesNotThrow(() -> dispatcher.dispatch(null, Collections.emptyList(), 1));
        
        // Shutdown executor
        executor.shutdown();
        assertDoesNotThrow(() -> dispatcher.dispatch(mock(SimulationWorld.class), Collections.emptyList(), 1));
    }

    @Test
    @DisplayName("ParallelDispatcher: Process multiple services")
    void dispatcher_multiple_services() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ParallelDispatcher dispatcher = new ParallelDispatcher(executor);
        
        SimulationWorld world = mock(SimulationWorld.class);
        WorkUnit unit = mock(WorkUnit.class);
        SimulationNode node = mock(SimulationNode.class);
        
        when(world.getParallelWorkUnits()).thenReturn(Collections.singletonList(unit));
        when(unit.iterator()).thenReturn(Collections.singletonList(node).iterator());
        
        ParallelTask service1 = mock(ParallelTask.class);
        ParallelTask service2 = mock(ParallelTask.class);
        
        dispatcher.dispatch(world, Arrays.asList(service1, service2), 1);
        
        verify(service1).processCell(node, 1);
        verify(service2).processCell(node, 1);
        
        executor.shutdown();
    }

    @Test
    @DisplayName("ParallelDispatcher: Internal processor error handling")
    void dispatcher_processor_error() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ParallelDispatcher dispatcher = new ParallelDispatcher(executor);
        
        SimulationWorld world = mock(SimulationWorld.class);
        WorkUnit unit = mock(WorkUnit.class);
        SimulationNode node = mock(SimulationNode.class);
        
        when(world.getParallelWorkUnits()).thenReturn(Collections.singletonList(unit));
        when(unit.iterator()).thenReturn(Collections.singletonList(node).iterator());
        
        ParallelTask service = mock(ParallelTask.class);
        doThrow(new RuntimeException("Parallel Fail")).when(service).processCell(any(), anyInt());
        
        // Should not throw exception to caller
        assertDoesNotThrow(() -> dispatcher.dispatch(world, Collections.singletonList(service), 1));
        
        executor.shutdown();
    }

    @Test
    @DisplayName("ParallelDispatcher: Pool capacity management")
    void dispatcher_pool_capacity() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ParallelDispatcher dispatcher = new ParallelDispatcher(executor);
        SimulationWorld world = mock(SimulationWorld.class);
        
        // Initial run with 2 units
        WorkUnit unit1 = mock(WorkUnit.class);
        WorkUnit unit2 = mock(WorkUnit.class);
        when(world.getParallelWorkUnits()).thenReturn(Arrays.asList(unit1, unit2));
        when(unit1.iterator()).thenReturn(Collections.emptyIterator());
        when(unit2.iterator()).thenReturn(Collections.emptyIterator());
        
        dispatcher.dispatch(world, Collections.emptyList(), 1);
        
        // Run with 1 unit (should shrink)
        when(world.getParallelWorkUnits()).thenReturn(Collections.singletonList(unit1));
        dispatcher.dispatch(world, Collections.emptyList(), 1);
        
        // Run with 3 units (should grow)
        WorkUnit unit3 = mock(WorkUnit.class);
        when(world.getParallelWorkUnits()).thenReturn(Arrays.asList(unit1, unit2, unit3));
        when(unit3.iterator()).thenReturn(Collections.emptyIterator());
        dispatcher.dispatch(world, Collections.emptyList(), 1);
        
        executor.shutdown();
    }
}
