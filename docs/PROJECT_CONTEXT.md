# Project Context: Island Ecosystem Simulator

## Status: Intelligent & Spatial-Aware (v1.68.0)
The system now features **Intelligent Animal Behavior** and a **Hardened Frontend Architecture**.

## Project Goal
To provide a high-performance, extensible engine for simulating complex ecosystems and urban environments, leveraging modern Java features and ECS architecture.

## System State (Summary)
- **Engine**: Stable, **Zero-GC hot path** (v1.63.0+). Mutation score 69% (PITest).
- **Intelligence**: `AnimalMovementSystem` uses `SpatialIndex` and `SenseComponent` for predatory/prey heuristics.
- **Performance**: Expanded **JMH benchmarks** cover both Nature (SoA) and SimCity (Connectivity/Population) domains.
- **Domains**: Nature and SimCity are integrated and performant at 20x20 scale.
- **Backend**: Spring Boot 3.2.5 (Security disabled) with **Persistent H2 Storage**. Snapshots survive restarts.
- **Frontend**: Vite + React 18. **Hardened Architecture**: Decomposed components, unified state management, and externalized styling (v1.68.0).
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
