package com.island.engine.scheduling;

import com.island.engine.core.SimulationWorld;
import com.island.engine.internal.ParallelDispatcher;
import com.island.engine.internal.PhaseScheduler;
import com.island.engine.model.Mortal;
import com.island.engine.model.Tickable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameLoopComprehensiveTest {

    @Test
    @DisplayName("GameLoop: Should throw exception if started without world")
    void shouldThrowIfNoWorld() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        GameLoop<Mortal> loop = new GameLoop<>(10, executor, mock(PhaseScheduler.class));
        assertThrows(IllegalStateException.class, loop::start);
        executor.shutdown();
    }

    @Test
    @DisplayName("GameLoop: Should handle stop condition and callback")
    void shouldHandleStopCondition() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        PhaseScheduler<Mortal> scheduler = mock(PhaseScheduler.class);
        GameLoop<Mortal> loop = new GameLoop<>(10, executor, scheduler);
        loop.setWorld(mock(SimulationWorld.class));

        AtomicInteger stopCount = new AtomicInteger(0);
        loop.setStopCondition(() -> loop.getTickCount() >= 5);
        loop.setOnStopCallback(stopCount::incrementAndGet);

        loop.start();
        assertTrue(loop.awaitStop(1, TimeUnit.SECONDS));
        
        assertTrue(loop.getTickCount() >= 5);
        assertEquals(1, stopCount.get());
        assertEquals(SimulationStatus.IDLE, loop.getStatus());
        
        executor.shutdown();
    }

    @Test
    @DisplayName("GameLoop: Should handle different task registration overloads")
    void shouldHandleTaskRegistration() {
        PhaseScheduler<Mortal> scheduler = mock(PhaseScheduler.class);
        GameLoop<Mortal> loop = new GameLoop<>(10, mock(ExecutorService.class), scheduler);
        
        Tickable tickable = mock(Tickable.class);
        Runnable runnable = mock(Runnable.class);
        ScheduledTask scheduledTask = mock(ScheduledTask.class);
        
        loop.addRecurringTask(tickable);
        loop.addRecurringTask(runnable);
        loop.addRecurringTask(scheduledTask);
        
        // Internal runTick will drain pendingTasks
        loop.runTick();
        
        verify(scheduler).execute(any(), argThat(list -> list.size() == 3), anyInt(), anyLong());
    }

    @Test
    @DisplayName("GameLoop: Should handle world tick exceptions")
    void shouldHandleWorldTickExceptions() {
        PhaseScheduler<Mortal> scheduler = mock(PhaseScheduler.class);
        GameLoop<Mortal> loop = new GameLoop<>(10, mock(ExecutorService.class), scheduler);
        SimulationWorld<Mortal> world = mock(SimulationWorld.class);
        doThrow(new RuntimeException("World Boom")).when(world).tick(anyInt());
        loop.setWorld(world);

        assertDoesNotThrow(loop::runTick);
        verify(scheduler).execute(any(), anyList(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("GameLoop: Should handle awaitStop timeout")
    void shouldHandleAwaitStopTimeout() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        GameLoop<Mortal> loop = new GameLoop<>(1000, executor, mock(PhaseScheduler.class));
        loop.setWorld(mock(SimulationWorld.class));
        
        loop.start();
        // Await for a very short time while it's sleeping/running
        assertFalse(loop.awaitStop(10, TimeUnit.MILLISECONDS));
        
        loop.stop();
        assertTrue(loop.awaitStop(2, TimeUnit.SECONDS));
        executor.shutdown();
    }
}