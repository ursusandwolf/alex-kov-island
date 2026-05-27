package com.island.service;

import com.island.config.SimulationProperties;
import com.island.controller.SimulationType;
import com.island.engine.core.NamedSimulationPlugin;
import com.island.engine.core.SimulationContext;
import com.island.engine.core.SimulationEngine;
import com.island.engine.core.SimulationPlugin;
import com.island.engine.scheduling.GameLoop;
import com.island.engine.scheduling.SimulationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.island.engine.event.EventBus;
import com.island.engine.model.Mortal;
import com.island.engine.scheduling.GameLoop;
import com.island.engine.scheduling.SimulationStatus;
import com.island.util.common.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimulationServiceAtomicRestartTest {

    private SimulationService simulationService;
    private ApplicationEventPublisher eventPublisher;
    private SimulationEngine simulationEngine;
    private NamedSimulationPlugin naturePlugin;
    private SimulationContext<Mortal> oldContext;
    private SimulationContext<Mortal> newContext;
    private GameLoop<Mortal> oldLoop;
    private GameLoop<Mortal> newLoop;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        simulationEngine = mock(SimulationEngine.class);
        naturePlugin = mock(NamedSimulationPlugin.class);
        when(naturePlugin.getPluginName()).thenReturn("nature");
        
        oldLoop = mock(GameLoop.class);
        when(oldLoop.getStatus()).thenReturn(SimulationStatus.RUNNING);
        oldContext = new SimulationContext<>(
                mock(com.island.engine.core.SimulationWorld.class),
                oldLoop,
                mock(RandomProvider.class),
                mock(EventBus.class),
                mock(ExecutorService.class)
        );

        newLoop = mock(GameLoop.class);
        newContext = new SimulationContext<>(
                mock(com.island.engine.core.SimulationWorld.class),
                newLoop,
                mock(RandomProvider.class),
                mock(EventBus.class),
                mock(ExecutorService.class)
        );
        
        simulationService = new SimulationService(eventPublisher, List.of(naturePlugin), new SimulationProperties(), simulationEngine);
    }

    @Test
    void shouldKeepOldContextIfNewOneFailsToBuild() {
        // 1. Manually set an "old" context using a first successful start
        when(naturePlugin.withConfiguration(anyInt(), anyInt(), any())).thenReturn(mock(SimulationPlugin.class));
        when(simulationEngine.build(any(), any())).thenReturn(oldContext);
        
        simulationService.start(SimulationType.NATURE, 20, 20, 100);
        assertEquals(SimulationStatus.RUNNING, simulationService.getStatus());

        // 2. Mock a failure for the next start
        when(simulationEngine.build(any(), any())).thenThrow(new RuntimeException("Build failed"));

        // 3. Attempt restart - should fail but keep old context
        assertThrows(RuntimeException.class, () -> simulationService.start(SimulationType.NATURE, 20, 20, 100));
        
        // 4. Verify old context is still there and its loop was NOT stopped (by checking close() side effects if we could, 
        // but here we just check if status is still RUNNING from oldLoop)
        assertEquals(SimulationStatus.RUNNING, simulationService.getStatus());
        verify(oldLoop, never()).stop();
    }

    @Test
    void shouldReplaceOldContextIfNewOneSucceeds() {
        // 1. Start first
        when(naturePlugin.withConfiguration(anyInt(), anyInt(), any())).thenReturn(mock(SimulationPlugin.class));
        when(simulationEngine.build(any(), any())).thenReturn(oldContext);
        simulationService.start(SimulationType.NATURE, 20, 20, 100);

        // 2. Mock second success
        when(simulationEngine.build(any(), any())).thenReturn(newContext);
        when(newLoop.getStatus()).thenReturn(SimulationStatus.RUNNING);

        // 3. Restart
        simulationService.start(SimulationType.NATURE, 20, 20, 100);

        // 4. Verify swap and old close (which calls stop on loop)
        assertEquals(SimulationStatus.RUNNING, simulationService.getStatus());
        verify(oldLoop).stop();
        verify(newLoop).start();
    }
}
