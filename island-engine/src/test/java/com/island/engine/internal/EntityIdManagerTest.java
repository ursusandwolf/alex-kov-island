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
        Set<Long> ids = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            assertTrue(ids.add(manager.getNextId()));
        }
    }

    @Test
    @DisplayName("EntityIdManager: Should recycle IDs")
    void shouldRecycleIds() {
        EntityIdManager manager = new EntityIdManager();
        long id1 = manager.getNextId();
        long id2 = manager.getNextId();
        
        manager.releaseId(id1);
        long id3 = manager.getNextId();
        
        assertEquals(id1, id3, "Should recycle the first released ID");
        assertNotEquals(id2, id3);
    }

    @Test
    @DisplayName("EntityIdManager: Should handle multiple releases")
    void shouldHandleMultipleReleases() {
        EntityIdManager manager = new EntityIdManager();
        long id1 = manager.getNextId();
        long id2 = manager.getNextId();
        long id3 = manager.getNextId();
        
        manager.releaseId(id1);
        manager.releaseId(id2);
        
        long next1 = manager.getNextId();
        long next2 = manager.getNextId();
        
        // Order of recycling depends on implementation (likely LIFO or FIFO)
        Set<Long> recycled = Set.of(next1, next2);
        assertTrue(recycled.contains(id1));
        assertTrue(recycled.contains(id2));
    }
}