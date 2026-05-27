import { create } from 'zustand';
import { PopulationPoint, WorldSnapshot } from '../types/simulation';

interface SimulationState {
  snapshot: WorldSnapshot | null;
  populationHistory: PopulationPoint[];
  viewingHistory: boolean;
  connected: boolean;
  setSnapshot: (snapshot: WorldSnapshot | null) => void;
  setLiveSnapshot: (snapshot: WorldSnapshot | null) => void;
  addPopulationPoint: (point: PopulationPoint) => void;
  setConnected: (connected: boolean) => void;
  setViewingHistory: (viewing: boolean) => void;
  exitHistoryView: () => void;
  resetPopulationHistory: () => void;
}

export const useSimulationStore = create<SimulationState>((set) => ({
  snapshot: null,
  populationHistory: [],
  viewingHistory: false,
  connected: false,

  setSnapshot: (snapshot) => set({ snapshot }),
  
  setLiveSnapshot: (snapshot) => set((state) => {
    if (state.viewingHistory) return state;
    return { snapshot };
  }),

  addPopulationPoint: (point) => set((state) => {
    // Keep last 50 points
    const newHistory = [...state.populationHistory, point].slice(-50);
    return { populationHistory: newHistory };
  }),

  setConnected: (connected) => set({ connected }),
  
  setViewingHistory: (viewingHistory) => set({ viewingHistory }),

  exitHistoryView: () => set({ viewingHistory: false }),

  resetPopulationHistory: () => set({ populationHistory: [] }),
}));
