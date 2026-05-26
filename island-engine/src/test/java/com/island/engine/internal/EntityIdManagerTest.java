package com.island.engine.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityIdManagerTest {

    @Test
    @DisplayName("EntityIdManager: Should generate unique IDs")
    void shouldGenerateUniqueIds() {
        EntityIdManager manager = new EntityIdManager();
        Set<Integer> ids = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            assertTrue(ids.add(manager.acquireId()));
        }
    }

    @Test
    @DisplayName("EntityIdManager: Should recycle IDs")
    void shouldRecycleIds() {
        EntityIdManager manager = new EntityIdManager();
        int id1 = manager.acquireId();
        int id2 = manager.acquireId();
        
        manager.releaseId(id1);
        int id3 = manager.acquireId();
        
        assertEquals(id1, id3, "Should recycle the first released ID");
        assertNotEquals(id2, id3);
    }

    @Test
    @DisplayName("EntityIdManager: Should handle multiple releases")
    void shouldHandleMultipleReleases() {
        EntityIdManager manager = new EntityIdManager();
        int id1 = manager.acquireId();
        int id2 = manager.acquireId();
        int id3 = manager.acquireId();
        
        manager.releaseId(id1);
        manager.releaseId(id2);
        
        int next1 = manager.acquireId();
        int next2 = manager.acquireId();
        
        // Order of recycling depends on implementation (likely LIFO or FIFO)
        Set<Integer> recycled = Set.of(next1, next2);
        assertTrue(recycled.contains(id1));
        assertTrue(recycled.contains(id2));
    }
}