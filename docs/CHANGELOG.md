# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.76.0] - 2026-05-27

### Added
- **Repository Rename**: Renamed repository to `alex-kov-island`.
- **Battery Optimization Mode**: Introduced configuration presets for low-power operation.
  - Reduced default map size to 10x10.
  - Limited parallel execution to 2 threads.
  - Increased tick duration to 500ms and reduced broadcast frequency.

### Fixed
- **Frontend Compilation**: Fixed missing `PopulationPoint` type in `island-ui/src/types/simulation.ts` that prevented production builds.
- **Database Resilience**: Forcefully resolved H2 database locking issues during rapid application restarts.
- **Project Integrity**: Ensured all modules are properly installed in the local Maven repository to resolve inter-module dependencies.

## [1.75.0] - 2026-05-27

### Added
- **Canvas Tooltips**: Implemented real-time tooltips on the world map.
  - Automatic coordinate and species detection under the cursor.
  - Accounts for current zoom level and pan offset.
  - Visual feedback for empty vs populated cells.
  - Shared `Tooltip` UI component for consistent styling.
- **Enhanced Interaction**: Added a new instruction hint to the canvas area.

## [1.74.0] - 2026-05-27

### Added
- **Synthesized Audio Feedback**: Added audio cues for notifications using Web Audio API.
  - Success: Cheerful rising notes.
  - Error: Low-frequency warning buzz.
  - Info/Warning: Subtle clicks/pings.
  - No external assets required; sounds are generated dynamically.
- **Improved UX**: Enhanced user awareness of background simulation events.

## [1.73.0] - 2026-05-27

### Added
- **Toast Notifications**: Implemented a global notification system using Zustand and React.
  - Success, Error, Warning, and Info types with distinct icons and colors.
  - Automatic dismissal after 4 seconds or manual dismissal on click.
  - Integrated into all simulation actions (start, stop, pause, resume, save).
  - Smooth slide-in animations and theme-aware styling.

## [1.72.0] - 2026-05-27

### Added
- **Dark Mode**: Implemented full support for dark and light themes.
  - CSS Variables for all colors, backgrounds, and shadows.
  - Theme toggler in the `Header` with persistence in `localStorage`.
  - Theme-aware species colors for better visibility on dark backgrounds.
- **Visual Polish**: Improved card shadows, border radii, and spacing across the entire application.

## [1.71.0] - 2026-05-27

### Added
- **Shared UI Library**: Created a set of reusable atomic components in `src/shared/ui`:
  - `Button`: Flexible button with variants, sizes, and built-in loading states.
  - `Panel`: Standardized container for UI sections with headers and actions.
  - `Input`: Labeled input components for configuration.
- **Design System**: Centralized UI logic and styles, reducing CSS duplication.

### Changed
- **Component Refactoring**: All simulation components (`SimulationControls`, `SnapshotHistoryPanel`, `CellDetails`, etc.) now use shared UI components.
- **Visual Improvements**: Added animated loading indicators to all asynchronous buttons.

## [1.70.0] - 2026-05-27

### Added
- **Interactive Canvas**: Implemented Zoom & Pan functionality for `WorldCanvas`.
  - Mouse Wheel to zoom towards pointer.
  - Click & Drag to pan the world view.
  - High-DPI (Retina) support for crisp rendering.
  - Zoom info and "Reset View" toolbar.
- **Improved Visuals**: Added a wrapper and hints for the simulation canvas.

## [1.69.0] - 2026-05-27

### Changed
- **Frontend State Management**: Unified state management by migrating all API calls and mutations to **TanStack Query**. 
- **Zustand Store**: Simplified `useSimulationStore.ts` to only handle real-time WebSocket data and UI state (history view toggle).
- **Atomic Selectors**: Implemented atomic selectors in components to optimize re-renders.
- **App Component**: Further simplified `App.tsx` by removing manual status polling and improving coordinate parsing.

### Fixed
- **Redundant API Calls**: Removed duplicated API logic between Zustand store and TanStack Query hooks.
- **Coordination Safety**: Improved split/parse logic for selected coordinates to prevent crashes on invalid data.

## [1.68.0] - 2026-05-26

### Fixed
- **Frontend Refactoring**: Decomposed `App.tsx` into specialized sub-components (`Header`, `CellDetails`, `Legend`) for better maintainability.
- **Style Modernization**: Externalized almost all inline styles to `App.css`, improving CSS reusability and component readability.
- **Store Optimization**: Unified API error handling in `useSimulationStore.ts` using a `wrapApi` helper, reducing code duplication.
- **React Best Practices**: Removed deprecated `React.FC` usage across all frontend components.
- **Semantic HTML**: Improved accessibility and structure using semantic elements like `ul`/`li` for legends.

## [1.67.0] - 2026-05-26

### Added
- **Intelligent Animal Behavior**: Refined `AnimalMovementSystem` to use informed decision-making based on sensed environment.
- **Predatory/Prey Heuristics**: Animals now prioritize fleeing from significant threats (>50% hunt chance) and moving towards identified prey.
- **Directional Movement**: Implemented `moveTowards` and `moveAwayFrom` logic that respects animal speed and grid boundaries.
- **Data-Driven Senses**: Added `visionRadius` and `hearingRadius` to `species.properties` for all animal types.
- **Sense Components**: Animals are now initialized with `SenseComponent` via `NatureComponentFactory` based on their species-specific sensory capabilities.

## [1.66.0] - 2026-05-26

### Added
- **Spatial Indexing Framework**: Introduced `SpatialIndex` interface in `island-engine` for efficient proximity queries.
- **GridSpatialIndex**: High-performance grid-based spatial index implementation for O(1) cell-level neighbor access.
- **QuadTree**: Added a generic `QuadTree` utility for future non-grid spatial partitioning.
- **Animal Sensing**: Integrated `SpatialIndex` into `AnimalMovementSystem` in `island-nature`, enabling animals with `SenseComponent` to perceive nearby prey.

### Fixed
- **SimCity**: Fixed `CityMap.getNode()` which was returning `Optional.empty()`, breaking relative neighbor lookups.

## [1.65.0] - 2026-05-26

### Added
- **Performance Benchmarking**: Expanded JMH benchmarks to cover `island-simcity` domain logic.
- **SimCity Benchmarks**: Added comprehensive benchmarks for `ConnectivityService` (BFS propagation), `PopulationService`, `PollutionService`, `ZoningService`, and `EconomySystem`.
- **Benchmark Coverage**: Supports varying map sizes (20x20 to 100x100) and resident densities (1 to 20 per tile) to identify scalability bottlenecks.

### Changed
- **Dependency Management**: Updated project version to 1.65.0. Added `island-simcity` dependency to `island-benchmarks`.

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
