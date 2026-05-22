import { WorldSnapshot } from '../types/simulation';

// Domain model - clean, UI-optimized version of the snapshot
export interface SimulationSnapshot {
  ticks: number;
  area: { width: number; height: number };
  entityCount: number;
  metrics: Record<string, number>;
  grid: {
    coordinates: string;
    isPlant: boolean;
    hasOrganisms: boolean;
  }[][];
}

export const mapSnapshotToDomain = (dto: WorldSnapshot): SimulationSnapshot => ({
  ticks: dto.tickCount,
  area: { width: dto.width, height: dto.height },
  entityCount: dto.totalEntityCount,
  metrics: dto.metrics,
  grid: dto.nodes.map(row => row.map(node => ({
    coordinates: node.coordinates,
    isPlant: node.topSpeciesPlant,
    hasOrganisms: node.hasOrganisms,
  }))),
});
