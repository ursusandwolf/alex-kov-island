# Technical Documentation: Island Ecosystem Simulator

## Architecture Overview
The system follows a **Modular Monolith** approach with strict separation of concerns using **JPMS (Java Platform Module System)** and **ECS (Entity-Component-System)** patterns.

### Layers (Hexagonal Influence)
1.  **Core Engine (`island-engine`)**: Independent of domain logic. Manages the lifecycle, scheduling (Phase-based), and SoA-based storage.
2.  **Domain Plugins (`island-nature`, `island-simcity`)**: Implement specific simulation rules via `SimulationPlugin` and `EntitySystem`.
3.  **Application (`island-app`)**: Spring Boot-managed host. Orchestrates plugins, provides REST/WebSocket APIs, and manages persistence.

## Visualization & Analysis
### UML Diagrams
Detailed pseudographic class diagrams illustrating the relationship between the Engine core and Domain plugins are maintained in **[docs/UML.md](./UML.md)**.
Frontend component hierarchy and state flow diagrams are available in **[docs/UI_UML.md](./UI_UML.md)**.

### Frontend Architecture (island-ui)
Frontend построен на компонентном подходе с четким разделением ответственности за состояние:
- **Server State**: TanStack Query управляет статусом симуляции, списком снимков и операциями управления (`start`, `stop`, `save`).
- **Real-time State**: Zustand хранит текущий `WorldSnapshot`, историю популяции для графиков и состояние подключения к WebSocket.
- **Hooks**: Бизнес-логика вынесена в кастомные хуки (`useSimulationQueries`, `useSimulationSocket`), что делает компоненты визуально чистыми.

### Code Review Insights (v1.77.0)
- **Concurrency (Backend)**: Использование `GridUtils.executeWithDoubleLock` для безопасной миграции сущностей.
- **Efficiency (Frontend)**: Использование `refetchInterval: connected ? false : 3000` в `useSimulationStatus` предотвращает лишние запросы при активном WebSocket-подключении.
- **Decoupling (Frontend)**: WebSocket-логика полностью изолирована в `useSimulationSocket`, что позволяет менять транспорт без изменения UI.
- **Optimization Opportunities**: Выявлены аллокации `Optional` в `Island.getNode` (Backend), требующие перехода на Zero-GC.

## Implementation Standards
- **Java 21**: Utilizing Virtual Threads (via `ParallelDispatcher`) and Sealed Classes where applicable.
- **ECS Pattern**: Entities are just IDs; data is stored in SoA (Structure of Arrays) for cache-friendly access.
- **Strategy Pattern**: Used for extensibility (e.g., `SocialEffectProvider` in SimCity).
- **Lombok**: Ubiquitous use for boilerplate reduction.
- **Resource Management**: Support for low-power/battery-saving modes via `application.yml` (reduced map size, thread capping, and increased tick intervals).

## Database & Infrastructure
- **Persistence**: JPA with **persistent H2 storage** (`jdbc:h2:file:./data/simulations_db`). Snapshots are stored as JSON CLOBs.
- **Observability**: Spring Boot Actuator + Micrometer + Prometheus.
- **Frontend**: Vite + React 18. **Unified State Management**: TanStack Query for server state/mutations, Zustand for real-time WebSocket updates. Atomic selectors and decomposed components.
- **Containerization**: Optimized multi-stage Docker build with dependency caching. Produces a lean JRE-based image.

## API Specification (v1)
All endpoints are prefixed with `/api/v1/simulation`.

### REST Endpoints
- `POST /start`: Initialize and start a simulation with custom parameters.
- `POST /start-from-snapshot`: Start a new simulation from a saved snapshot.
- `POST /stop`: Gracefully stop the current simulation.
- `POST /pause` / `POST /resume`: Control the execution flow.
- `GET /status`: Current engine state (`IDLE`, `RUNNING`, `PAUSED`).
- `GET /snapshot`: Return the current in-memory world snapshot.
- `POST /snapshot/save`: Persist the current state to snapshot history.
- `GET /snapshot/history`: List saved snapshot filenames.
- `GET /snapshot/history/{filename}`: Load a saved historical snapshot.

### WebSocket (STOMP)
- **Topic**: `/topic/world-state`
- **Payload**: `WorldSnapshot` (Polymorphic JSON).
- **Broadcast Interval**: Configurable via `sim.broadcast-interval`.

## Testing & Quality Strategy
- **Unit/Integration**: JUnit 5 + Mockito.
- **Mutation Testing**: PITest (Target: 60%+ score) integrated into CI.
- **Property-based**: `jqwik` for complex domain invariant verification.
- **ArchUnit**: Enforcing JPMS and layer boundaries.
- **Compatibility**: `Revapi` for API surface tracking.

## Performance Benchmarking
The project includes a dedicated `island-benchmarks` module using **JMH (Java Microbenchmark Harness)** to track hot path performance and identify regressions.

### Key Benchmarks
1.  **SoA vs. Map**: Comparing Structure of Arrays storage with standard `HashMap` for component access.
2.  **SimCity Hot Paths**:
    *   `ConnectivityService`: BFS-based propagation of road, water, and power networks.
    *   `PopulationService`: Scaling of resident logic with increasing entity density.
    *   `Full Tick`: End-to-end simulation cycle performance across different map sizes (20x20 to 100x100).
