package com.island.nature.service;

import com.island.engine.core.SpatialIndex;
import com.island.engine.ecs.Component;
import com.island.nature.entities.components.MetabolismComponent;
import com.island.nature.entities.components.MovementComponent;
import com.island.nature.entities.components.SenseComponent;
import com.island.nature.entities.core.Animal;
import com.island.nature.entities.core.AnimalType;
import com.island.nature.entities.core.DeathCause;
import com.island.nature.entities.core.Organism;
import com.island.nature.entities.domain.NatureWorld;
import com.island.nature.entities.domain.TaskRegistry;
import com.island.nature.model.Cell;
import com.island.nature.model.Island;
import com.island.util.common.RandomProvider;
import com.island.util.sampling.SamplingContext;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * ECS System responsible for animal movement with performance sampling.
 */
public class AnimalMovementSystem extends NatureEntitySystem {

    public AnimalMovementSystem(NatureWorld world, ExecutorService executor, RandomProvider random) {
        super(world, executor, random);
    }

    @Override
    public List<Class<? extends Component>> readComponents() {
        return List.of(MovementComponent.class, SenseComponent.class);
    }

    @Override
    public List<Class<? extends Component>> writeComponents() {
        return List.of(MetabolismComponent.class);
    }

    @Override
    public int priority() {
        return TaskRegistry.PRIORITY_MOVEMENT;
    }

    @Override
    protected void doProcessCell(Cell cell, int tickCount) {
        // Animals use sampling to maintain performance
        cell.forEachAnimalSampled(new SamplingContext(config.getMovementLodLimit(), getRandom()), animal -> {
            // Re-verify components due to sampling potentially bypassing query filter if used directly
            if (animal.getComponent(MovementComponent.class) != null && animal.getComponent(MetabolismComponent.class) != null) {
                process(animal, cell, tickCount);
            }
        });
    }

    @Override
    protected void process(Organism entity, Cell cell, int tickCount) {
        Animal animal = (Animal) entity;
        if (!animal.isAlive()) {
            return;
        }

        if (shouldAct(animal, AnimalType.Action.MOVE, tickCount)) {
            int speed = animal.getSpeed();
            
            if (protectionMap != null && protectionMap.containsKey(animal.getSpeciesKey())) {
                speed += config.getEndangeredSpeedBonus();
            }
            
            if (speed > 0) {
                Cell target = selectTargetCell(animal, cell, speed);
                if (target != null && target != cell) {
                    if (((NatureWorld) getWorld()).moveOrganism(animal, cell, target)) {
                        long moveCost = (animal.getMaxEnergy() * (1 + speed) * config.getSpeedMoveCostStepBP()) / config.getScale10K();
                        animal.consumeEnergy(moveCost);
                        if (!animal.isAlive()) {
                            animal.die(DeathCause.MOVEMENT_EXHAUSTION);
                        }
                    }
                }
            }
        }
    }

    private Cell selectTargetCell(Animal animal, Cell node, int speed) {
        SenseComponent sense = animal.getComponent(SenseComponent.class);
        if (sense != null && sense.getVisionRadius() > 0) {
            Cell sensedTarget = senseEnvironment(animal, node, sense.getVisionRadius(), speed);
            if (sensedTarget != null) {
                return sensedTarget;
            }
        }

        // For low speed or very small grids, neighbors are more reliable
        if (speed == 1 || (config.getIslandWidth() <= 3 && config.getIslandHeight() <= 3)) {
            List<Cell> neighbors = node.getCellNeighbors();
            if (!neighbors.isEmpty()) {
                int choice = getRandom().nextInt(neighbors.size() + 1);
                return (choice < neighbors.size()) ? neighbors.get(choice) : node;
            }
        }
        
        // Try to find a valid jump target
        for (int i = 0; i < 5; i++) {
            int dx = getRandom().nextInt(-speed, speed + 1);
            int dy = getRandom().nextInt(-speed, speed + 1);
            if (dx == 0 && dy == 0) {
                continue;
            }
            
            Cell target = ((Island) getWorld()).getCellOrNull(node, dx, dy);
            if (target != null) {
                return target;
            }
        }
        
        // Fallback to neighbors if jump failed
        List<Cell> neighbors = node.getCellNeighbors();
        if (!neighbors.isEmpty()) {
            return neighbors.get(getRandom().nextInt(neighbors.size()));
        }

        return node;
    }

    private Cell senseEnvironment(Animal animal, Cell node, int radius, int speed) {
        // Find best target in radius
        Cell bestPreyCell = null;
        Cell bestThreatCell = null;
        int minPreyDist = Integer.MAX_VALUE;
        int minThreatDist = Integer.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                Cell target = ((NatureWorld) getWorld()).getCell(node, dx, dy).orElse(null);
                if (target != null) {
                    int dist = Math.max(Math.abs(dx), Math.abs(dy)); // Chebyshev distance for grid
                    
                    if (containsThreat(animal, target)) {
                        if (dist < minThreatDist) {
                            minThreatDist = dist;
                            bestThreatCell = target;
                        }
                    } else if (containsPrey(animal, target)) {
                        if (dist < minPreyDist) {
                            minPreyDist = dist;
                            bestPreyCell = target;
                        }
                    }
                }
            }
        }

        if (bestThreatCell != null) {
            return moveAwayFrom(node, bestThreatCell, speed);
        }
        if (bestPreyCell != null) {
            return moveTowards(node, bestPreyCell, speed);
        }
        return null;
    }

    private boolean containsThreat(Animal prey, Cell cell) {
        final boolean[] found = {false};
        cell.forEachAnimal(predator -> {
            if (found[0]) return;
            int chance = ((NatureWorld) getWorld()).getInteractionProvider().getChance(predator.getSpeciesKey(), prey.getSpeciesKey());
            if (chance > 50) { // Significant threat
                found[0] = true;
            }
        });
        return found[0];
    }

    private boolean containsPrey(Animal predator, Cell cell) {
        final boolean[] found = {false};
        cell.forEachAnimal(animal -> {
            if (found[0]) return;
            int chance = ((NatureWorld) getWorld()).getInteractionProvider().getChance(predator.getSpeciesKey(), animal.getSpeciesKey());
            if (chance > 0) {
                found[0] = true;
            }
        });
        return found[0];
    }

    private Cell moveTowards(Cell current, Cell target, int speed) {
        int dx = Integer.compare(target.getX(), current.getX());
        int dy = Integer.compare(target.getY(), current.getY());
        
        int moveX = dx * Math.min(speed, Math.abs(target.getX() - current.getX()));
        int moveY = dy * Math.min(speed, Math.abs(target.getY() - current.getY()));
        
        return ((NatureWorld) getWorld()).getCell(current, moveX, moveY).orElse(current);
    }

    private Cell moveAwayFrom(Cell current, Cell threat, int speed) {
        int dx = Integer.compare(current.getX(), threat.getX());
        int dy = Integer.compare(current.getY(), threat.getY());
        
        // If we can't move away (at boundary), try to move sideways
        Cell escape = ((NatureWorld) getWorld()).getCell(current, dx * speed, dy * speed).orElse(null);
        if (escape == null || escape == current) {
            // Try 8 directions to find ANY cell further from threat
            for (int i = 0; i < 8; i++) {
                int rdx = getRandom().nextInt(-1, 2);
                int rdy = getRandom().nextInt(-1, 2);
                Cell alt = ((NatureWorld) getWorld()).getCell(current, rdx * speed, rdy * speed).orElse(null);
                if (alt != null && distSq(alt, threat) > distSq(current, threat)) {
                    return alt;
                }
            }
        }
        return escape != null ? escape : current;
    }

    private double distSq(Cell c1, Cell c2) {
        int dx = c1.getX() - c2.getX();
        int dy = c1.getY() - c2.getY();
        return dx * dx + dy * dy;
    }
}
