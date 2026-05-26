package com.island.nature.entities.strategy;

import com.island.nature.config.Configuration;
import com.island.nature.model.Cell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.island.engine.core.SimulationNode;
import com.island.nature.entities.core.Animal;
import com.island.nature.entities.core.Biomass;
import com.island.nature.entities.core.Organism;
import com.island.nature.entities.core.SpeciesKey;
import com.island.util.common.RandomProvider;
import com.island.nature.model.InteractionProvider;

/**
 * Provider for prey selection within a node using integer arithmetic.
 */
public class PreyProvider {
    private Cell node;
    private Configuration config;
    private InteractionProvider matrix;
    private int currentTick;
    private Map<SpeciesKey, Integer> protectionMap;
    private boolean isWolfPack;
    private RandomProvider random;

    private final List<Organism> buffet = new ArrayList<>(64);
    private final Map<SpeciesKey, Organism> uniquePrey = new HashMap<>(32);

    public PreyProvider() {
        // For pooling
    }

    public PreyProvider(Cell node, InteractionProvider matrix, 
                        int currentTick, Map<SpeciesKey, Integer> protectionMap, RandomProvider random) {
        this(node, matrix, currentTick, protectionMap, false, random);
    }

    public PreyProvider(Cell node, InteractionProvider matrix, 
                        int currentTick, Map<SpeciesKey, Integer> protectionMap, 
                        boolean isWolfPack, RandomProvider random) {
        update(node, matrix, currentTick, protectionMap, isWolfPack, random);
    }

    public void update(Cell node, InteractionProvider matrix, 
                       int currentTick, Map<SpeciesKey, Integer> protectionMap, 
                       boolean isWolfPack, RandomProvider random) {
        this.node = node;
        this.config = node != null ? node.getConfig() : null;
        this.matrix = matrix;
        this.currentTick = currentTick;
        this.protectionMap = protectionMap;
        this.isWolfPack = isWolfPack;
        this.random = random;
    }

    public List<Organism> getPreyFor(Animal predator) {
        buildBuffet(predator);
        
        // Strategy: prefer prey that gives more energy relative to its weight/size
        buffet.sort(Comparator.comparingLong(Organism::getWeight).reversed());
        
        return buffet;
    }

    private void buildBuffet(Animal predator) {
        buffet.clear();
        uniquePrey.clear();
        boolean canHuntAsPack = isWolfPack && predator.getAnimalType().isPackHunter();

        // 1. Animals - group by species
        node.forEachAnimal(a -> {
            if (a != predator && a.isAlive() && !uniquePrey.containsKey(a.getSpeciesKey())) {
                int baseChance = matrix.getChance(predator.getSpeciesKey(), a.getSpeciesKey());
                boolean canHunt = baseChance > 0;

                if (!canHunt && canHuntAsPack && config != null && a.getWeight() > 150 * config.getScale1M()) {
                    canHunt = true;
                }

                if (canHunt && !a.isProtected(currentTick)) {
                    uniquePrey.put(a.getSpeciesKey(), a);
                }
            }
        });

        buffet.addAll(uniquePrey.values());

        // 2. Plants/Biomass
        node.forEachEntity(e -> {
            if (e instanceof Biomass b && b.getBiomass() > 0 && matrix.getChance(predator.getSpeciesKey(), b.getSpeciesKey()) > 0) {
                if (!isPlantProtected(b)) {
                    buffet.add(b);
                }
            }
        });
        
        Collections.shuffle(buffet);
    }

    private boolean isPlantProtected(Biomass plant) {
        if (protectionMap == null) {
            return false;
        }
        Integer hideChance = protectionMap.get(plant.getSpeciesKey());
        return hideChance != null && random.nextInt(0, 100) < hideChance;
    }

    public void markAsHiding(Animal prey) {
        prey.setHiding(true);
    }

    public void markAsEaten(Organism prey) {
        // Handled by node.removeEntity
    }
}