package com.island.simcity;

import com.island.engine.core.SimulationContext;
import com.island.engine.core.SimulationEngine;
import com.island.engine.model.WorldSnapshot;
import com.island.simcity.entities.SimEntity;
import com.island.simcity.model.CityMap;
import com.island.simcity.model.CitySnapshot;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimCitySnapshotTest {

    @Test
    void testCreateSnapshot() {
        SimulationEngine<SimEntity> engine = new SimulationEngine<>();
        SimulationContext<SimEntity> context = engine.build(new SimCityPlugin(5, 5), 0, 1);
        
        CityMap map = (CityMap) context.world();
        
        // After fix, this should return a valid snapshot
        WorldSnapshot snapshot = map.createSnapshot();
        
        assertNotNull(snapshot, "Snapshot should NOT be null after the fix");
        assertTrue(snapshot instanceof CitySnapshot, "Snapshot should be an instance of CitySnapshot");
        assertEquals(5, snapshot.getWidth());
        assertEquals(5, snapshot.getHeight());
        assertEquals(0, snapshot.getTickCount());
        
        context.gameLoop().stop();
    }
}
