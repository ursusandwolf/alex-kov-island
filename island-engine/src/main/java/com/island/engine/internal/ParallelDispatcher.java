package com.island.engine.internal;

import com.island.engine.core.InternalEngine;
import com.island.engine.core.ParallelTask;
import com.island.engine.core.SimulationNode;
import com.island.engine.core.SimulationWorld;
import com.island.engine.core.WorkUnit;
import com.island.engine.model.Mortal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles parallel execution of CellServices across world work units.
 * <p>
 * NOTE: This class is NOT thread-safe for concurrent calls to {@link #dispatch}.
 * The internal processor pool is optimized for single-threaded management from the GameLoop.
 *
 * @param <T> The base type of entities.
 */
@InternalEngine
@Slf4j
@RequiredArgsConstructor
public final class ParallelDispatcher<T extends Mortal> {

    private final List<CellProcessor<T>> processorPool = new ArrayList<>();
    private final List<CellProcessor<T>> activeProcessors = new ArrayList<>();
    private final ExecutorService taskExecutor;

    public void dispatch(SimulationWorld<T> world, List<ParallelTask<T>> services, int tickCount) {
        if (world == null || taskExecutor.isShutdown()) {
            return;
        }

        for (ParallelTask<T> service : services) {
            try {
                service.beforeTick(tickCount);
            } catch (Exception e) {
                log.error("Error in beforeTick for service {}: {}", service.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        Collection<? extends WorkUnit<T>> workUnits = world.getParallelWorkUnits();
        int unitCount = workUnits.size();
        
        if (unitCount > 0) {
            ensurePoolCapacity(unitCount);

            activeProcessors.clear();
            int i = 0;
            for (WorkUnit<T> unit : workUnits) {
                CellProcessor<T> processor = processorPool.get(i++);
                processor.update(unit, services, tickCount);
                activeProcessors.add(processor);
            }

            // Using CountDownLatch instead of invokeAll to avoid Future list allocation
            final CountDownLatch latch = new CountDownLatch(unitCount);
            for (CellProcessor<T> processor : activeProcessors) {
                taskExecutor.execute(() -> {
                    try {
                        processor.call();
                    } catch (Exception e) {
                        log.error("Error in parallel task execution", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    log.warn("Parallel execution timed out after 10s");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parallel execution interrupted", e);
            }
        }

        for (ParallelTask<T> service : services) {
            try {
                service.afterTick(tickCount);
            } catch (Exception e) {
                log.error("Error in afterTick for service {}: {}", service.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    private void ensurePoolCapacity(int unitCount) {
        if (processorPool.size() < unitCount) {
            while (processorPool.size() < unitCount) {
                processorPool.add(new CellProcessor<>());
            }
        } else if (processorPool.size() > unitCount) {
            while (processorPool.size() > unitCount) {
                processorPool.remove(processorPool.size() - 1);
            }
        }
    }

    private static class CellProcessor<T extends Mortal> implements Callable<Void> {
        private WorkUnit<T> unit;
        private List<ParallelTask<T>> services;
        private int tickCount;

        void update(WorkUnit<T> unit, List<ParallelTask<T>> services, int tickCount) {
            this.unit = unit;
            this.services = services;
            this.tickCount = tickCount;
        }

        @Override
        public Void call() throws Exception {
            long start = System.nanoTime();
            try {
                for (SimulationNode<T> node : unit) {
                    for (ParallelTask<T> service : services) {
                        try {
                            service.processCell(node, tickCount);
                        } catch (Exception e) {
                            log.error("Error processing cell {} in service {}: {}", 
                                    node.getCoordinates(), service.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    }
                }
                return null;
            } finally {
                long duration = System.nanoTime() - start;
                unit.setLastExecutionTimeNanos(duration);
            }
        }
    }
}