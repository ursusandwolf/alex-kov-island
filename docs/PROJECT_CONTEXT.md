# Project Context: Island Ecosystem Simulator

## Status: Quality Hardening & Battery Optimization (v1.76.0)
The system is now stable and optimized for mobile/battery-powered development. Recent fixes resolved critical frontend build errors and database locking issues.

### 🎯 Current Focus
- [DONE] **Frontend Build Fix**: Restored production build capability by fixing missing TS types.
- [DONE] **Battery Saver Mode**: Reduced simulation load (10x10 grid, 2 threads, 500ms tick) for battery-efficient development.
- [DONE] **Project Integrity**: Resolved inter-module Maven dependency issues via local installation.
- [PENDING] **Zero-GC Hot Path Refinement**: Further reduce allocations in SimCity domain logic.
- [PENDING] **Spatial Indexing**: Implement QuadTree-based neighbor search for non-grid entities.

### 🏗 Architecture Notes
- **Modular Monolith**: Strict JPMS separation.
- **ECS/SoA**: High-performance data storage in the engine.
- **Reactive UI**: Zustand + TanStack Query with Zoom/Pan Canvas.
- **Persistence**: JPA/H2 file-based storage for simulation history.

### 🧪 Recent Changes (June 1, 2026)
- **Code Review**: Conducted deep dive into Engine, Nature, and UI core (v1.77.0).
- **UML**: Generated comprehensive pseudographic class diagrams in `docs/UML.md` (Backend) and `docs/UI_UML.md` (Frontend).
- **Interview Prep**: Compiled a list of architectural questions and project summary.
- **Optimization Strategy**: Identified `Optional` allocations in `Island` hot paths as next refactoring target.
- **Docs**: Synchronized CHANGELOG, DOCUMENTATION, and UML sets. Fully pushed to `origin/dev`.

### 📝 Next Steps
- [ ] Refactor `Island.getNode` and `Island.getCell` to avoid `Optional` (Zero-GC effort).
- [ ] Implement global temperature and moisture cycles (Climate System Phase 2).
- [ ] Refine "Sense" logic to include seeking water and mating partners.
- [ ] Expand SimCity benchmarks to cover high-density scenarios.
