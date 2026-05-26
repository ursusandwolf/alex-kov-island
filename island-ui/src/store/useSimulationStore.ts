import { create } from 'zustand';
import { simulationApi } from '../api/simulationApi';
import { SimulationStatus, WorldSnapshot } from '../types/simulation';

interface SimulationState {
  status: SimulationStatus;
  snapshot: WorldSnapshot | null;
  error: string | null;
  history: string[];
  viewingHistory: boolean;
  connected: boolean;
  setSnapshot: (snapshot: WorldSnapshot | null) => void;
  setLiveSnapshot: (snapshot: WorldSnapshot | null) => void;
  setStatus: (status: SimulationStatus) => void;
  setError: (error: string | null) => void;
  setConnected: (connected: boolean) => void;
  exitHistoryView: () => void;
  start: (type: 'nature' | 'simcity', width?: number, height?: number, tickMs?: number) => Promise<void>;
  startFromSnapshot: (filename: string, type: 'nature' | 'simcity', tickMs?: number) => Promise<void>;
  pause: () => Promise<void>;
  resume: () => Promise<void>;
  stop: () => Promise<void>;
  updateStatus: () => Promise<void>;
  fetchHistory: () => Promise<void>;
  saveSnapshot: () => Promise<void>;
  loadHistoricalSnapshot: (filename: string) => Promise<void>;
}

export const useSimulationStore = create<SimulationState>((set) => ({
  status: 'IDLE',
  snapshot: null,
  error: null,
  history: [],
  viewingHistory: false,
  connected: false,
  setSnapshot: (snapshot) => set({ snapshot }),
  setLiveSnapshot: (snapshot) => set((state) => state.viewingHistory ? state : { snapshot }),
  setStatus: (status) => set({ status }),
  setError: (error) => set({ error }),
  setConnected: (connected) => set({ connected }),
  exitHistoryView: () => set({ viewingHistory: false }),

  updateStatus: async () => {
    try {
      const data = await simulationApi.getStatus();
      set({ status: data.status, error: null });
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  start: async (type, width = 20, height = 20, tickMs = 100) => {
    try {
      const response = await simulationApi.start(type.toUpperCase(), width, height, tickMs);
      if (!response.ok) {
        throw new Error(await response.text() || response.statusText);
      }
      set({ viewingHistory: false, error: null });
      await useSimulationStore.getState().updateStatus();
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  startFromSnapshot: async (filename, type, tickMs = 100) => {
    try {
      const response = await simulationApi.startFromSnapshot(filename, type.toUpperCase(), tickMs);
      if (!response.ok) {
        throw new Error(await response.text() || response.statusText);
      }
      set({ viewingHistory: false, error: null });
      await useSimulationStore.getState().updateStatus();
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  pause: async () => {
    try {
      const response = await simulationApi.pause();
      if (!response.ok) {
        throw new Error(await response.text() || response.statusText);
      }
      await useSimulationStore.getState().updateStatus();
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  resume: async () => {
    try {
      const response = await simulationApi.resume();
      if (!response.ok) {
        throw new Error(await response.text() || response.statusText);
      }
      await useSimulationStore.getState().updateStatus();
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  stop: async () => {
    try {
      const response = await simulationApi.stop();
      if (!response.ok) {
        throw new Error(await response.text() || response.statusText);
      }
      await useSimulationStore.getState().updateStatus();
      set({ snapshot: null, viewingHistory: false, error: null });
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  fetchHistory: async () => {
    try {
      const data = await simulationApi.getHistory();
      set({ history: data.filenames || [], error: null });
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  saveSnapshot: async () => {
    try {
      const response = await simulationApi.saveSnapshot();
      if (!response.ok) {
        throw new Error(await response.text() || response.statusText);
      }
      await useSimulationStore.getState().fetchHistory();
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },

  loadHistoricalSnapshot: async (filename) => {
    try {
      const snapshot = await simulationApi.getHistoricalSnapshot(filename);
      set({ snapshot, error: null, viewingHistory: true });
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  },
}));
