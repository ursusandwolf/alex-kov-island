# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.64.0] - 2026-05-26

### Added
- **Mutation Testing**: Integrated PITest into the build process and CI pipeline.
- **Core Engine Tests**: Added comprehensive unit tests for `SystemExecutionGraph`, `PhaseScheduler`, `GameLoop`, and `EntityIdManager`.
- **Quality Gates**: Established a 60% mutation threshold baseline for the project.

### Changed
- **Testing Infrastructure**: Enabled `mock-maker-inline` to support mocking of `final` engine classes.
- **Dependency Management**: Centralized PITest configuration in the parent `pom.xml`.

### Fixed
- **SystemExecutionGraph**: Fixed a scheduling bug where independent systems could jump over their priority-based dependencies into earlier batches, potentially causing data inconsistency.

## [1.63.0] - 2026-05-26

### Optimized
- **Zero-GC Hot Path**: Eliminated significant object allocations in the simulation hot loop, reducing GC pause times by up to 18x.
- **ParallelDispatcher**: Replaced `ExecutorService.invokeAll()` with a manual `CountDownLatch` implementation to avoid recurring `List<Future>` allocations.
- **AnimalFeedingSystem**: Introduced `ThreadLocal` scratchpads for `PreyProvider` and temporary animal lists, removing per-cell and per-animal allocations during feeding logic.
- **DefaultEventBus**: Optimized event publishing by caching type hierarchies as arrays, avoiding iterator allocations during high-frequency events (birth/death).
- **PreyProvider**: Refactored to be reusable and mutable, supporting object pooling for zero-allocation prey selection.

### Added
- Comprehensive Multithreading Profiling Report in `docs/testing/PROFILING_REPORT.md`.

## [1.62.0] - 2026-05-26

### Fixed
- **SimCity Domain**: Implemented `CityMap.createSnapshot()` which previously returned `null`, enabling snapshots, WebSocket broadcasts, and persistence for the SimCity domain.
- **Persistence Reliability**: Hardened snapshot filename generation by adding milliseconds and a random suffix, and added a unique constraint to the database to prevent collisions.
- **Atomic Lifecycle**: Refactored `SimulationService` to make simulation restarts atomic; the old simulation now continues running if the new context fails to build or start.

### Added
- New test suites: `SimCitySnapshotTest` and `SimulationServiceAtomicRestartTest` to verify architectural robustness.

## [1.61.0] - 2026-05-26

### Added
- **Persistence Hardening**: Configured H2 file-based datasource (`jdbc:h2:file:./data/simulations_db`) to ensure simulation snapshots and history persist across application restarts.

### Changed
- **Docker Optimization**: Refactored `Dockerfile` to use a separate layer for Maven dependencies (`mvn dependency:go-offline`), significantly reducing build times by leveraging Docker layer caching.
- **Frontend Performance**: Optimized `useSimulationStatus` TanStack Query hook to use polling only as a fallback when the WebSocket connection is lost, reducing unnecessary HTTP load during active sessions.
- **State Management**: Centralized WebSocket connection status in the global Zustand store to allow coordination between different frontend hooks and components.

## [1.60.0] - 2026-05-25

### Added
- Type-safe `getDefaultPluginType()` in `SimulationProperties` to improve plugin initialization.
- Repeatable multithread profiling workflow in `docs/testing/MULTITHREAD_PROFILING.md` with `scripts/profile-multithreading.sh`.

### Fixed
- **Architectural Cleanup**: Removed multiple Fully Qualified Names (FQNs) in code bodies across `island-engine`, `island-nature`, and `island-simcity` modules, adhering to style guidelines in `GEMINI.md`.
- **Configuration**: Removed unnecessary `volatile` modifiers in `SimulationProperties` where standard Spring `ConfigurationProperties` behavior is sufficient.
- **Simulation Lifecycle**: `SimulationService.stop()` now fully releases the active context instead of leaving executors alive after manual stop.
- **Graceful Shutdown**: `GameLoop.stop()` no longer interrupts the current tick, preventing noisy shutdown errors during normal stop/restart flows.
- **Frontend State Isolation**: Historical snapshots are no longer overwritten by live WebSocket updates until the user explicitly returns to live mode.
- **Test Runtime**: Mockito test modules now use the subclass mock maker, avoiding JVM self-attach failures on Java 21 environments without inline agent support.

### Changed
- Refactored `SimulationService` to use new type-safe property accessors.
- Verified that `SnapshotHistoryService` successfully transitioned to JPA-based persistence, rendering `historyDir` property obsolete.

## [1.59.0] - 2026-05-22

### Changed
- Refactored `SimulationService` to use constructor-based dependency injection for `SimulationEngine`, removing direct instantiation.
- Updated `SimulationBeanConfig` to define `SimulationEngine` as a Spring Bean.
- Adjusted `module-info.java` to explicitly open internal packages to `spring-boot`, `spring-core`, and `org.hibernate.orm.core` to resolve runtime access errors in tests.
- Fixed `SimulationControllerTest` assertions and mock setup to accommodate dependency injection changes.

## [1.58.0] - 2026-05-21

### Added
- Case-insensitive `SimulationType` resolution via `@JsonCreator` and custom `Converter` in `WebConfig`, allowing frontend to use lowercase domain names.
- Debug logging in `SimulationBroadcaster` and `GameLoop` for better visibility of real-time activity.

### Changed
- **Frontend Refactoring**: Migrated STOMP logic to a dedicated `useSimulationSocket` hook and decoupled it from the global Zustand store for better lifecycle management.
- Centralized all simulation API calls in `simulationApi.ts`.
- Optimized default simulation size to 20x20 and increased broadcast frequency to 1 tick/update.

### Fixed
- **Critical Deadlock**: Resolved a non-recursive locking issue in `Cell.java` where `StampedLock` caused threads to hang during entity movement. Refactored `forEach*` methods to release locks before executing domain logic.
- Resolved "Internal server error" during simulation start caused by enum case mismatch.
- Fixed frontend runtime crash in `SnapshotHistoryPanel` by correctly handling the structured `SnapshotListResponse`.

### Removed
- **Spring Security**: Completely removed `spring-boot-starter-security` and all related configurations to eliminate persistent 403 Forbidden errors and the "Sign In" modal.

## [1.57.0] - 2026-05-19

### Added
- Introduced `SimulationType` enum to replace string-based types for domain selection (NATURE, SIMCITY), improving type-safety across the API and service layer.
- Added `SnapshotListResponse` record DTO for the `/snapshot/history` endpoint to return structured data containing the list of filenames and their total count.

### Changed
- Refactored `SimulationService` lifecycle methods (`start`, `startFromSnapshot`) to use the new `SimulationType` enum.
- Clarified single-user concurrent simulation constraint in Swagger `@Tag` and `SimulationService` Javadoc.

### Removed
- Removed the deprecated and unused `historyDir` field from `SimulationProperties`, fully completing the transition to JPA-backed snapshot storage.

## [1.56.0] - 2026-05-15

### Added
- **Spring Security** integration with Basic Authentication for simulation control and management endpoints.
- **JPA Persistence** with H2 database for simulation snapshot history, replacing filesystem storage.
- **SocialEffectProvider** strategy pattern in `island-simcity` for OCP-compliant building effects.
- **Observability** stack with multi-stage `Dockerfile` and `docker-compose.yml` including Prometheus.
- **Property-based testing** with `jqwik` for `AnimalHealthSystem` (1000 trials/property).
- **API Compatibility** checks via Revapi integrated into the build lifecycle.

### Fixed
- Resolved race conditions in `SocialService` by migrating `CityTile` level metrics to `AtomicInteger`.
- Fixed visibility issues in `SimulationProperties` by adding `volatile` to dynamically tuned fields.

## [1.55.0] - 2026-05-14

### Added
- Integrated **Spring Boot Actuator** with **Micrometer Prometheus** registry.
- Added no-args constructors and getters/setters to all `WorldSnapshot` implementations (`IslandSnapshot`, `CitySnapshot`, etc.) for robust polymorphic serialization.

### Changed
- Refactored `CitySnapshot` to be fully domain-agnostic by copying data from `CityMap` into serializable fields.
- Updated `SimulationBroadcaster` to use standard Spring property placeholder syntax (`${...}`) for scheduled tasks.
- Translated `Main.java` Javadoc to English to maintain codebase consistency.

### Fixed
- Resolved **JPMS** `InaccessibleObjectException` by opening `com.island.config` and domain model packages to required modules and reflection.
- Fixed a flaky integration test in `SimulationServiceIntegrationTest` by adding `await()` to ensure simulation start before lifecycle actions.
- Resolved polymorphic serialization failures in `SnapshotHistoryServiceTest` and enabled the test.
- Fixed `NaturePlugin` JPMS service provider requirement by adding a manual public no-args constructor.

## [1.54.0] - 2026-05-13

### Added
- Seeding simulation world from historical snapshots in `SimulationService`, `NaturePlugin`, and `SimCityPlugin`.
- Enhanced `SimulationBroadcaster` with dynamic tick interval and asynchronous STOMP broadcasting.
- New `SocialService` for Education and Health in `island-simcity`.
- Evolution mechanics for residents based on Education Quotient (EQ) and Health.
- High-Tech industrial zones transition logic.

### Changed
- Refactored `SimulationService` to use a plugin registry, eliminating if/else logic (OCP compliance).
- Cleaned up repository by removing tracked `node_modules` and updating `.gitignore`.
- Refactored `SimulationControllerTest` to use `@WebMvcTest` for faster execution.
- `SnapshotHistoryService.loadSnapshot` now returns `Optional<WorldSnapshot>`.

### Fixed
- Resolved TOCTOU NPE race condition in `SimulationService` lifecycle methods.
- Fixed React UI O(W×H) rendering bottleneck in the dashboard.
- Restored simulation broadcasting build integrity.

## [1.53.0] - 2026-05-12

### Added
- REST API v1 for simulation control and snapshot management.
- WebSocket STOMP broadcasting for real-time world state visualization.
- React-based Dashboard with World Canvas and Simulation Controls.
- `SnapshotHistoryService` for persistence of simulation states to JSON files.

### Changed
- Migrated application to Spring Boot framework.
- Centralized configuration using `SimulationProperties` and Spring Profiles.

## [1.50.0] - 2026-05-11

### Added
- Multi-threaded simulation engine with phase-based scheduling.
- Entity-Component-System (ECS) architecture with SoA storage.
- Nature simulation domain with metabolic and predatory logic.
- SimCity simulation domain with RCI zone mechanics and desirability.
- JPMS module isolation for core engine and domain plugins.
