# Project Context: Island Ecosystem Simulator

## Status: Hardened & Optimized (v1.61.0)
The system is now "v14-Review-Ready" with hardened persistence, optimized Docker builds, and efficient frontend polling.

## Project Goal
To provide a high-performance, extensible engine for simulating complex ecosystems and urban environments, leveraging modern Java features and ECS architecture.

## System State (Summary)
- **Engine**: Stable, Zero-GC hot path, now with non-blocking cell traversal.
- **Domains**: Nature and SimCity are integrated and performant at 20x20 scale.
- **Backend**: Spring Boot 3.2.5 (Security disabled) with **Persistent H2 Storage**. Snapshots now survive restarts.
- **Frontend**: Vite + React 18. **Intelligent Polling**: TanStack Query now only polls when WebSocket is disconnected.
- **Infrastructure**: Optimized **multi-stage Dockerfile** with dependency caching. Prometheus pre-configured for real-time monitoring.

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
3.  **Quality Hardening**:
    *   Expand `jqwik` property-based tests to cover entity movement and reproduction race conditions.
    *   Implement Mutation Testing (PITest) to verify test suite effectiveness.
