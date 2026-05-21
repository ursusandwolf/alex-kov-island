import { create } from 'zustand';
import { WorldSnapshot, SimulationStatus } from '../types/simulation';
import { simulationApi } from '../api/simulationApi';

interface SimulationState {
  status: SimulationStatus;
  snapshot: WorldSnapshot | null;
  error: string | null;
  history: string[];

  // Setters
  setSnapshot: (snapshot: WorldSnapshot | null) => void;
  setStatus: (status: SimulationStatus) => void;
  setError: (error: string | null) => void;
  
  // Actions
  start: (type: 'nature' | 'simcity', width?: number, height?: number, tickMs?: number) => Promise<void>;
  startFromSnapshot: (filename: string, type: 'nature' | 'simcity', tickMs?: number) => Promise<void>;
  pause: () => Promise<void>;
  resume: () => Promise<void>;
  stop: () => Promise<void>;
  updateStatus: () => Promise<void>;
  
  // History Actions
  fetchHistory: () => Promise<void>;
  saveSnapshot: () => Promise<void>;
  loadHistoricalSnapshot: (filename: string) => Promise<void>;
}

export const useSimulationStore = create<SimulationState>((set) => ({
  status: 'IDLE',
  snapshot: null,
  error: null,
  history: [],

  setSnapshot: (snapshot) => set({ snapshot }),
  setStatus: (status) => set({ status }),
  setError: (error) => set({ error }),

  updateStatus: async () => {
    try {
      const data = await simulationApi.getStatus();
      set({ status: data.status, error: null });
    } catch (err) {
      set({ error: String(err) });
    }
  },

  start: async (type, width = 20, height = 20, tickMs = 100) => {
    try {
      const res = await simulationApi.start(type.toUpperCase(), width, height, tickMs);
      if (!res.ok) throw new Error(`Start failed: ${await res.text()}`);
      await simulationApi.getStatus();
    } catch (err) {
      set({ error: String(err) });
    }
  },

  startFromSnapshot: async (filename, type, tickMs = 100) => {
    try {
      const res = await simulationApi.startFromSnapshot(filename, type.toUpperCase(), tickMs);
      if (!res.ok) throw new Error(`Start from snapshot failed: ${await res.text()}`);
      await simulationApi.getStatus();
    } catch (err) {
      set({ error: String(err) });
    }
  },

  pause: async () => {
    try {
      const res = await simulationApi.pause();
      if (!res.ok) throw new Error(`Pause failed: ${res.statusText}`);
      await useSimulationStore.getState().updateStatus();
    } catch (err) {
      set({ error: String(err) });
    }
  },

  resume: async () => {
    try {
      const res = await simulationApi.resume();
      if (!res.ok) throw new Error(`Resume failed: ${res.statusText}`);
      await useSimulationStore.getState().updateStatus();
    } catch (err) {
      set({ error: String(err) });
    }
  },

  stop: async () => {
    try {
      const res = await simulationApi.stop();
      if (!res.ok) throw new Error(`Stop failed: ${res.statusText}`);
      await useSimulationStore.getState().updateStatus();
      set({ snapshot: null });
    } catch (err) {
      set({ error: String(err) });
    }
  },

  fetchHistory: async () => {
    try {
      const data = await simulationApi.getHistory();
      set({ history: data.filenames || [], error: null });
    } catch (err) {
      set({ error: String(err) });
    }
  },

  saveSnapshot: async () => {
    try {
      await simulationApi.saveSnapshot();
      // Refetch history
      const data = await simulationApi.getHistory();
      set({ history: data.filenames || [] });
    } catch (err) {
      set({ error: String(err) });
    }
  },

  loadHistoricalSnapshot: async (filename) => {
    try {
      const snapshot = await simulationApi.getHistoricalSnapshot(filename);
      set({ snapshot, error: null });
    } catch (err) {
      set({ error: String(err) });
    }
  }
}));
