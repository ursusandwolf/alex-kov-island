package com.island.service;

import com.island.controller.SimulationType;
import com.island.engine.scheduling.SimulationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
@ActiveProfiles("nature")
class SimulationServiceIntegrationTest {

    @Autowired
    private SimulationService simulationService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldManageSimulationLifecycle() throws Exception {
        await().atMost(2, SECONDS).until(() -> simulationService.getStatus() == SimulationStatus.RUNNING);
        assertNotNull(readContextField(), "default startup should create a simulation context");

        simulationService.pause();
        assertEquals(SimulationStatus.PAUSED, simulationService.getStatus());
        
        simulationService.resume();
        assertEquals(SimulationStatus.RUNNING, simulationService.getStatus());
        
        simulationService.stop();
        assertEquals(SimulationStatus.IDLE, simulationService.getStatus());
        assertNull(readContextField(), "manual stop should release the active simulation context");

        simulationService.start(SimulationType.NATURE, 20, 20, 100);
        await().atMost(2, SECONDS).until(() -> simulationService.getStatus() == SimulationStatus.RUNNING);
        assertNotNull(readContextField(), "simulation should be restartable after a full stop");
    }

    private Object readContextField() throws Exception {
        Field contextField = SimulationService.class.getDeclaredField("context");
        contextField.setAccessible(true);
        return contextField.get(simulationService);
    }
}
