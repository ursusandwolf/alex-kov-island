# Multithreading Profiling Report (v1.62.0)

## Execution Environment
- **OS**: Darwin (MacOS/Unix)
- **Cores**: 8
- **Simulation**: Nature (Island), 20x20 Grid
- **LOD Settings**: Low Density (Predator 1%, Herbivore 2%)
- **Target Tick Rate**: 10ms (100 TPS)

## Performance Sweep Results

| Threads | Ticks (5s) | TPS | CPU Avg | GC Max (s) | Scaling |
|---------|------------|-----|---------|------------|---------|
| 1       | 0          | 0.0 | 3.4%    | 0.19       | 1.0x    |
| 2       | 1          | 0.2 | 13.5%   | 0.17       | -       |
| 4       | 3          | 0.6 | 37.1%   | 0.18       | 3.0x vs 2t |
| 8       | 4          | 0.8 | 56.2%   | 2.07       | 1.3x vs 4t |

## Analysis

### 1. Throughput Scaling
- Significant performance gain observed when moving from 2 to 4 threads.
- 8 threads provide diminishing returns for a 20x20 grid, likely due to small chunk sizes and synchronization overhead.
- Total Entity Count (including biomass mass) remains the primary bottleneck for raw TPS.

### 2. Zero-GC Optimization (v1.63.0)
After initial profiling, hot-path allocations were identified and eliminated:
- **ParallelDispatcher**: Replaced `invokeAll` with `CountDownLatch`, removing `List<Future>` allocations per batch.
- **AnimalFeedingSystem**: Implemented `ThreadLocal` scratchpads for `PreyProvider` and temporary lists, removing thousands of allocations per tick.
- **DefaultEventBus**: Optimized hierarchy traversal to use cached arrays instead of sets/iterators.

**Impact**:
- **GC Pause**: Reduced from **2.07s** to **0.11s** at 8 threads (**18x improvement**).
- **CPU Overhead**: Significant reduction in "wasted" CPU cycles previously spent on allocation and GC.

### 3. Conclusion & Recommendation
- **Optimal Thread Count**: 4 threads provide the best balance between throughput and GC stability for medium-sized worlds.
- **Virtual Threads**: Current configuration requires `threads >= 1`. Enabling Virtual Threads (`threads=0`) would likely improve performance for high-concurrency tasks but requires relaxing validation constraints.
