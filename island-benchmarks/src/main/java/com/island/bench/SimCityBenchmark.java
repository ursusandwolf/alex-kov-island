package com.island.bench;

import com.island.engine.ecs.ComponentRegistry;
import com.island.engine.event.EventBus;
import com.island.simcity.entities.SimEntity;
import com.island.simcity.entities.components.BuildingComponent;
import com.island.simcity.entities.components.PopulationComponent;
import com.island.simcity.model.CityMap;
import com.island.simcity.model.CityTile;
import com.island.simcity.service.ConnectivityService;
import com.island.simcity.service.PopulationService;
import com.island.simcity.service.PollutionService;
import com.island.simcity.service.ZoningService;
import com.island.simcity.service.EconomySystem;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks for SimCity domain logic.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class SimCityBenchmark {

    @Param({"20", "50", "100"})
    private int mapSize;

    @Param({"1", "5", "20"})
    private int residentsPerTile;

    private CityMap map;
    private ConnectivityService connectivityService;
    private PopulationService populationService;
    private PollutionService pollutionService;
    private ZoningService zoningService;
    private EconomySystem economySystem;

    @Setup
    public void setup() {
        ComponentRegistry registry = new ComponentRegistry();
        EventBus eventBus = EventBus.create();
        map = new CityMap(mapSize, mapSize, eventBus, registry);
        connectivityService = new ConnectivityService(map);
        populationService = new PopulationService(map, registry);
        pollutionService = new PollutionService(map);
        zoningService = new ZoningService(map);
        economySystem = new EconomySystem(map);

        // Fill the map with some infrastructure and population
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                CityTile tile = map.getGrid()[x][y];
                
                // Add a road to every tile to test BFS connectivity
                SimEntity road = new SimEntity(registry);
                road.addComponent(BuildingComponent.builder()
                        .type(BuildingComponent.Type.ROAD)
                        .build());
                tile.addEntity(road);

                // Add a residential building to every tile
                SimEntity house = new SimEntity(registry);
                house.addComponent(BuildingComponent.builder()
                        .type(BuildingComponent.Type.RESIDENTIAL)
                        .density(BuildingComponent.Density.MEDIUM)
                        .build());
                tile.addEntity(house);

                // Add some population to every tile
                for (int i = 0; i < residentsPerTile; i++) {
                    SimEntity resident = new SimEntity(registry);
                    resident.addComponent(PopulationComponent.builder()
                            .age(25)
                            .happiness(80)
                            .health(100)
                            .build());
                    tile.addEntity(resident);
                }
                
                // Add some industrial buildings to generate pollution
                if ((x + y) % 5 == 0) {
                    SimEntity factory = new SimEntity(registry);
                    factory.addComponent(BuildingComponent.builder()
                            .type(BuildingComponent.Type.INDUSTRIAL)
                            .build());
                    tile.addEntity(factory);
                }
            }
        }
        
        // Add a power plant at (0,0) to enable electricity propagation
        SimEntity powerPlant = new SimEntity(registry);
        powerPlant.addComponent(BuildingComponent.builder()
                .type(BuildingComponent.Type.POWER_PLANT)
                .build());
        map.getGrid()[0][0].addEntity(powerPlant);
        
        // Initial propagation
        connectivityService.beforeTick(0);
    }

    @Benchmark
    public void connectivityPropagation() {
        connectivityService.beforeTick(1);
    }

    @Benchmark
    public void populationProcessing() {
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                populationService.processCell(map.getGrid()[x][y], 1);
            }
        }
    }

    @Benchmark
    public void pollutionProcessing() {
        pollutionService.beforeTick(1);
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                pollutionService.processCell(map.getGrid()[x][y], 1);
            }
        }
    }

    @Benchmark
    public void zoningProcessing() {
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                // Zoning only runs every 5 ticks, but we want to benchmark its work
                zoningService.processCell(map.getGrid()[x][y], 5);
            }
        }
    }

    @Benchmark
    public void economyProcessing() {
        economySystem.beforeTick(1);
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                economySystem.processCell(map.getGrid()[x][y], 1);
            }
        }
        economySystem.afterTick(1);
    }

    @Benchmark
    public void fullTickSimulation() {
        // PREPARE phase
        connectivityService.beforeTick(1);
        pollutionService.beforeTick(1);
        economySystem.beforeTick(1);
        
        // SIMULATION phase (simplified)
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                CityTile tile = map.getGrid()[x][y];
                pollutionService.processCell(tile, 1);
                populationService.processCell(tile, 1);
                zoningService.processCell(tile, 5); // Force it to run
                economySystem.processCell(tile, 1);
            }
        }
        economySystem.afterTick(1);
    }
}
