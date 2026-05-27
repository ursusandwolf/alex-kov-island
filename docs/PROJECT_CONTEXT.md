# Project Context: Island Ecosystem Simulator

## Status: Frontend Overhaul (v1.75.0)
The system now features a **Modern, Reactive Frontend** with unified state management, interactive canvas (Zoom/Pan/Tooltips), and a shared UI library with Dark Mode support.

## Project Goal
To provide a high-performance, extensible engine for simulating complex ecosystems and urban environments, leveraging modern Java features and ECS architecture.

## System State (Summary)
- **Engine**: Stable, **Zero-GC hot path** (v1.63.0+). Mutation score 69% (PITest).
- **Intelligence**: `AnimalMovementSystem` uses `SpatialIndex` and `SenseComponent` for predatory/prey heuristics.
- **Performance**: Expanded **JMH benchmarks** cover both Nature (SoA) and SimCity (Connectivity/Population) domains.
- **Domains**: Nature and SimCity are integrated and performant at 20x20 scale.
- **Backend**: Spring Boot 3.2.5 (Security disabled) with **Persistent H2 Storage**. Snapshots survive restarts.
- **Frontend**: Vite + React 18. **Modern Architecture**: Unified TanStack Query + Zustand state, Shared UI library, Interactive Canvas (Zoom/Pan/Tooltips), Dark Mode, and Audio Feedback (v1.75.0).
- **Infrastructure**: Optimized **multi-stage Dockerfile** with dependency caching. Prometheus pre-configured.

## Technical Entry Point
For detailed architectural patterns, API specs, and implementation standards, refer to:
👉 **[DOCUMENTATION.md](DOCUMENTATION.md)**
👉 **[ARCHITECTURE_PRESENTATION.md](ARCHITECTURE_PRESENTATION.md)** (Interview/Demo Guide)

## Roadmap & Pending Items
1.  **Observability Phase 2**:
    *   Implement pre-configured Grafana dashboards for domain-specific metrics.
    *   Add ELK/Loki for structured logging analysis.
2.  **Domain Expansion**:
    *   Develop "Deep Sea" plugin with fluid dynamics and light-based metabolic cycles.
    *   Implement "Space" plugin for orbital mechanics and resource management.
3.  **Algorithmic Optimization**:
    *   [DONE] Implement Spatial Hashing/QuadTree for O(1) neighbor searches in Nature/SimCity.
    *   [DONE] Refine "Sense" logic to include fleeing from predators and seeking prey.
    *   Refine "Sense" logic to include seeking water and mating partners.
