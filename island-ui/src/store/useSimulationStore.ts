import { create } from 'zustand';
import { WorldSnapshot } from '../types/simulation';

interface SimulationState {
  snapshot: WorldSnapshot | null;
  setSnapshot: (snapshot: WorldSnapshot | null) => void;
}

export const useSimulationStore = create<SimulationState>((set) => ({
  snapshot: null,
  setSnapshot: (snapshot) => set({ snapshot }),
}));
