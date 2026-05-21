# Project Context: Island Ecosystem Simulator

## Status: Dashboard Operational (v1.58.0)
The dashboard is now fully functional with real-time updates. Critical deadlocks have been resolved, and the authentication layer has been removed to facilitate local development and unrestricted visualization.

## Project Goal
To provide a high-performance, extensible engine for simulating complex ecosystems and urban environments, leveraging modern Java features and ECS architecture.

## System State (Summary)
- **Engine**: Stable, Zero-GC hot path, now with non-blocking cell traversal.
- **Domains**: Nature and SimCity are integrated and performant at 20x20 scale.
- **Backend**: Spring Boot 3.2.5 (Security disabled) with JPA/H2 storage.
- **Frontend**: Vite + React 18 dashboard with centralized API and hook-based WebSocket management.

## Technical Entry Point
For detailed architectural patterns, API specs, and implementation standards, refer to:
👉 **[DOCUMENTATION.md](DOCUMENTATION.md)**

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
