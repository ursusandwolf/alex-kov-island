package com.island.nature.service;

import com.island.engine.core.SimulationNode;
import com.island.engine.ecs.Component;
import com.island.nature.entities.components.ConsumableComponent;
import com.island.nature.entities.components.HealthComponent;
import com.island.nature.entities.components.MetabolismComponent;
import com.island.nature.entities.core.Animal;
import com.island.nature.entities.core.AnimalType;
import com.island.nature.entities.core.DeathCause;
import com.island.nature.entities.core.Organism;
import com.island.nature.entities.core.SpeciesKey;
import com.island.nature.entities.domain.NatureWorld;
import com.island.nature.entities.domain.TaskRegistry;
import com.island.nature.entities.registry.AnimalFactory;
import com.island.nature.entities.registry.SpeciesRegistry;
import com.island.nature.entities.strategy.HuntingStrategy;
import com.island.nature.entities.strategy.PreyProvider;
import com.island.nature.model.Cell;
import com.island.util.common.RandomProvider;
import com.island.nature.model.InteractionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * ECS System responsible for animal feeding logic.
 * Replaces FeedingService.
 */
public class AnimalFeedingSystem extends NatureEntitySystem {
    private final AnimalFactory animalFactory;
    private final InteractionProvider interactionMatrix;
    private final SpeciesRegistry speciesRegistry;
    private final HuntingStrategy huntingStrategy;

    private static final ThreadLocal<Scratchpad> SCRATCHPAD = ThreadLocal.withInitial(Scratchpad::new);

    private static class Scratchpad {
        final List<Animal> packHunters = new ArrayList<>(32);
        final List<Animal> soloHunters = new ArrayList<>(64);
        final PreyProvider preyProvider = new PreyProvider();
    }

    public AnimalFeedingSystem(NatureWorld world, AnimalFactory animalFactory,
                               InteractionProvider interactionMatrix,
                               SpeciesRegistry speciesRegistry, HuntingStrategy huntingStrategy,
                               ExecutorService executor, RandomProvider random) {
        super(world, executor, random);
        this.animalFactory = animalFactory;
        this.interactionMatrix = interactionMatrix;
        this.speciesRegistry = speciesRegistry;
        this.huntingStrategy = huntingStrategy;
    }

    @Override
    public List<Class<? extends Component>> readComponents() {
        return List.of();
    }

    @Override
    public List<Class<? extends Component>> writeComponents() {
        return List.of(HealthComponent.class, MetabolismComponent.class);
    }

    @Override
    public int priority() {
        return TaskRegistry.PRIORITY_FEEDING;
    }

    @Override
    protected void doProcessCell(Cell cell, int tickCount) {
        Scratchpad scratch = SCRATCHPAD.get();
        processPredators(cell, tickCount, scratch);
        processHerbivores(cell, tickCount, scratch);
    }

    private void processPredators(Cell node, int tickCount, Scratchpad scratch) {
        scratch.packHunters.clear();
        scratch.soloHunters.clear();

        node.forEachPredator(p -> {
            if (p.getAnimalType().isPackHunter()) {
                scratch.packHunters.add(p);
            } else {
                if (p.isAlive() && shouldAct(p, AnimalType.Action.FEED, tickCount)) {
                    scratch.soloHunters.add(p);
                }
            }
        });

        // Process solo hunters
        for (Animal predator : scratch.soloHunters) {
            if (predator.isAlive()) {
                tryEat(predator, node, scratch);
            }
        }

        // Process pack hunters
        if (scratch.packHunters.size() >= config.getWolfPackMinSize()) {
            processPackHunting(scratch.packHunters, node, scratch);
        } else {
            for (Animal wolf : scratch.packHunters) {
                if (wolf.isAlive() && shouldAct(wolf, AnimalType.Action.FEED, tickCount)) {
                    tryEat(wolf, node, scratch);
                }
            }
        }
    }

    private void processHerbivores(Cell node, int tickCount, Scratchpad scratch) {
        node.forEachHerbivoreSampled(config.getFeedingLodLimit(), getRandom(), herbivore -> {
            if (herbivore.isAlive() && shouldAct(herbivore, AnimalType.Action.FEED, tickCount)) {
                tryEat(herbivore, node, scratch);
            }
        });
    }

    private void processPackHunting(List<Animal> pack, Cell node, Scratchpad scratch) {
        if (pack.isEmpty()) {
            return;
        }
        
        scratch.preyProvider.update(node, interactionMatrix, 0, protectionMap, true, getRandom());
        int maxKills = Math.max(1, pack.size() / 2);
        int kills = 0;
        int attempts = 0;
        int maxAttempts = 5;

        while (kills < maxKills && attempts < maxAttempts) {
            attempts++;
            Organism preyCandidate = huntingStrategy.selectPackPrey(pack, scratch.preyProvider);
            if (preyCandidate != null) {
                ConsumableComponent consumable = preyCandidate.getComponent(ConsumableComponent.class);
                if (consumable != null && consumable.isAnimal()) {
                    // Safe cast as it's an animal consumable
                    Animal actualPrey = findActualPrey(node, preyCandidate.getSpeciesKey(), pack.get(0));
                    if (actualPrey != null && actualPrey.isAlive() && !isProtected(actualPrey)) {
                        int baseChance = interactionMatrix.getChance(pack.get(0).getSpeciesKey(), actualPrey.getSpeciesKey());
                        int packChanceBP = huntingStrategy.calculatePackSuccessRate(pack, actualPrey, baseChance);
                        
                        if (getRandom().nextInt(0, config.getScale10K()) < packChanceBP) {
                            long gain = consumable.consume(actualPrey.getWeight());
                            if (node.removeEntity(actualPrey)) {
                                long gainPerWolf = gain / pack.size();
                                for (Animal wolf : pack) {
                                    if (wolf.isAlive()) {
                                        wolf.addEnergy(gainPerWolf);
                                    }
                                }
                                scratch.preyProvider.markAsEaten(actualPrey);
                                animalFactory.releaseAnimal(actualPrey);
                                kills++;
                            }
                        } else {
                            long strikeCost = huntingStrategy.calculateHuntCost(pack.get(0), actualPrey);
                            long penalty = (strikeCost * config.getPredatorFailHuntPenaltyBP()) / config.getScale10K();
                            for (Animal wolf : pack) {
                                wolf.consumeEnergy(penalty);
                            }
                        }
                    }
                }
            } else {
                break; 
            }
        }
    }

    private void tryEat(Animal consumer, Cell node, Scratchpad scratch) {
        if (consumer.getCurrentEnergy() >= consumer.getFoodForSaturation()) {
            return;
        }

        scratch.preyProvider.update(node, interactionMatrix, 0, protectionMap, false, getRandom());
        int attempts = 0;
        boolean success = false;
        boolean strikeAttempted = false;
        int maxAttempts = consumer.getAnimalType().isPredator() ? 5 : 3;

        while (consumer.getCurrentEnergy() < consumer.getFoodForSaturation() && attempts < maxAttempts) {
            attempts++;
            Organism preyCandidate = huntingStrategy.selectPrey(consumer, scratch.preyProvider);
            if (preyCandidate == null) {
                break;
            }
            
            ConsumableComponent consumable = preyCandidate.getComponent(ConsumableComponent.class);
            if (consumable == null) {
                continue;
            }

            strikeAttempted = true;
            if (consumable.isAnimal()) {
                Animal actualPrey = findActualPrey(node, preyCandidate.getSpeciesKey(), consumer);
                if (actualPrey != null && actualPrey.isAlive() && !isProtected(actualPrey)) {
                    int chance = interactionMatrix.getChance(consumer.getSpeciesKey(), actualPrey.getSpeciesKey());
                    int preyCount = node.getOrganismCount(actualPrey.getSpeciesKey());
                    if (preyCount > actualPrey.getAnimalType().getMaxPerCell() / 2) {
                        chance += config.getOverpopulationHuntBonusPercent(); 
                    }

                    if (getRandom().nextInt(0, 100) < chance) {
                        long gain = consumable.consume(actualPrey.getWeight());
                        if (node.removeEntity(actualPrey)) {
                            consumer.addEnergy(gain);
                            scratch.preyProvider.markAsEaten(actualPrey);
                            animalFactory.releaseAnimal(actualPrey);
                            success = true;
                        }
                    }
                }
            } else {
                // Biomass consumption
                if (!isPlantProtected(preyCandidate.getSpeciesKey())) {
                    long foodNeeded = consumer.getFoodForSaturation() - consumer.getCurrentEnergy();
                    consumer.addEnergy(consumable.consume(foodNeeded, node));
                    success = true;
                }
            }
        }
        
        if (!success && strikeAttempted) {
            long penaltyPercent = consumer.getAnimalType().isPredator() ? config.getPredatorFailHuntPenaltyBP() : config.getHerbivoreFailFeedPenaltyBP();
            consumer.consumeEnergy((consumer.getMaxEnergy() * penaltyPercent) / config.getScale10K());
        }
    }

    private Animal findActualPrey(Cell node, SpeciesKey speciesKey, Animal consumer) {
        AnimalType type = speciesRegistry.getAnimalType(speciesKey).orElse(null);
        if (type == null) {
            return null;
        }
        
        for (int i = 0; i < 3; i++) {
            Animal candidate = node.getRandomAnimalByType(type, getRandom());
            if (candidate != null && candidate != consumer) {
                return candidate;
            }
            if (candidate == null) {
                break;
            }
        }
        return null;
    }
}
