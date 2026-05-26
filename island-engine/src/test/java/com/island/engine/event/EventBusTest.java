package com.island.engine.event;


import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EventBusTest {

    @Test
    void shouldPublishToDirectSubscribers() {
        EventBus bus = EventBus.create();
        AtomicInteger count = new AtomicInteger(0);
        bus.subscribe(String.class, s -> count.incrementAndGet());

        bus.publish("Hello");
        assertEquals(1, count.get());
    }

    @Test
    void shouldPublishToHierarchicalSubscribers() {
        EventBus bus = EventBus.create();
        AtomicInteger count = new AtomicInteger(0);
        bus.subscribe(Object.class, o -> count.incrementAndGet());
        bus.subscribe(CharSequence.class, cs -> count.incrementAndGet());

        bus.publish("Hello");
        // String is both Object and CharSequence
        assertEquals(2, count.get());
    }

    @Test
    void shouldUnsubscribeCorrectly() {
        EventBus bus = EventBus.create();
        AtomicInteger count = new AtomicInteger(0);
        Consumer<String> subscriber = s -> count.incrementAndGet();
        
        bus.subscribe(String.class, subscriber);
        bus.publish("One");
        assertEquals(1, count.get());

        bus.unsubscribe(String.class, subscriber);
        bus.publish("Two");
        assertEquals(1, count.get());
    }

    @Test
    void shouldHandleSubscribersThrowingExceptions() {
        EventBus bus = EventBus.create();
        AtomicInteger count = new AtomicInteger(0);
        
        bus.subscribe(String.class, s -> { throw new RuntimeException("Fail"); });
        bus.subscribe(String.class, s -> count.incrementAndGet());

        bus.publish("Hello");
        // Second subscriber should still run
        assertEquals(1, count.get());
    }

    @Test
    void shouldCacheTypeHierarchy() {
        EventBus bus = EventBus.create();
        AtomicInteger count = new AtomicInteger(0);
        bus.subscribe(String.class, s -> count.incrementAndGet());

        // First publish populates cache
        bus.publish("Hello");
        assertEquals(1, count.get());

        // Second publish uses cache
        bus.publish("World");
        assertEquals(2, count.get());
    }

    @Test
    void shouldWorkWithWildcardSubscriber() {
        EventBus bus = EventBus.create();
        AtomicInteger count = new AtomicInteger(0);
        bus.subscribe(Object.class, o -> count.incrementAndGet());

        bus.publish("String");
        bus.publish(123);
        bus.publish(new Object());

        assertEquals(3, count.get());
    }

    @Test
    void shouldHandleUnsubscribeNonExistent() {
        EventBus bus = EventBus.create();
        Consumer<String> subscriber = s -> {};
        
        // Unsubscribe without subscribe should not throw
        assertDoesNotThrow(() -> bus.unsubscribe(String.class, subscriber));
    }
}