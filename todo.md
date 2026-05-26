# Island Ecosystem Simulator: Roadmap & TODO

## 🚀 Architectural Milestones (Completed)
- [x] **Event-Driven Core**: Decoupled domain logic using `EventBus`.
- [x] **ECS Evolution**: Advanced Entity-Component-System with SoA (Struct of Arrays) for performance.
- [x] **Parallelism**: `SystemExecutionGraph` and `ParallelDispatcher` for concurrent simulation phases.
- [x] **Modular Isolation**: Full JPMS integration with strict package exports.
- [x] **Observability**: Prometheus metrics, Spring Boot Actuator, and SpringDoc OpenAPI.
- [x] **Hardened Persistence**: JPA/H2 file-based storage for simulation history and snapshots.
- [x] **Docker Stack**: Multi-stage `Dockerfile` with dependency caching and `docker-compose.yml`.
- [x] **Frontend Architecture**: Decoupled React layers with Recharts dynamics (v1.69.0).
## 🛠 Active Quality Hardening
- [x] **Zero-GC Hot Path**: Optimize concurrent execution to minimize object allocations (v1.63.0).
- [x] **Performance Profiling**: Establish multithreaded performance baseline and identify scaling limits.
- [x] **Mutation Testing**: Setup PITest in CI pipeline to verify test effectiveness (v1.64.0).
- [x] **Spatial Indexing**: O(1) neighbor search using Spatial Hashing (v1.65.0).
- [x] **Benchmarking**: Expand JMH suites to cover SimCity domain logic (v1.66.0).
- [x] **App Review**: Fix `SIMCITY` snapshot flow in `island-app`
- [x] **Persistence**: Prevent snapshot filename collisions
- [x] **Lifecycle**: Make simulation restart atomic

## 📈 Future Vectors (Backlog)
- [ ] **Climate System**: Implement global temperature and moisture cycles affecting Nature and SimCity domains.
- [ ] **Intelligence Phase 2**: Refine "Sense" logic to include seeking water and mating partners.
- [ ] **Observability Phase 2**: Grafana dashboards and ELK/Loki integration.
- [ ] **Web UI 2.0**: Implementation of a 3D visualization using Three.js for the world canvas.
- [ ] **Auth Layer**: Re-introduce Spring Security with JWT/OAuth2 for multi-user simulation isolation.
- [ ] **Clustering**: Explore gRPC/Akka for distributed simulation across multiple JVM nodes.
