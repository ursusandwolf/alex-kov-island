import { create } from 'zustand';
import { simulationApi } from '../api/simulationApi';
import { SimulationStatus, WorldSnapshot } from '../types/simulation';

export interface PopulationPoint {
  tick: number;
  [key: string]: number; // speciesCode: count
}

interface SimulationState {
  status: SimulationStatus;
  snapshot: WorldSnapshot | null;
  error: string | null;
  history: string[];
  populationHistory: PopulationPoint[];
  viewingHistory: boolean;
  connected: boolean;
  setSnapshot: (snapshot: WorldSnapshot | null) => void;
  setLiveSnapshot: (snapshot: WorldSnapshot | null) => void;
  addPopulationPoint: (point: PopulationPoint) => void;
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

export const useSimulationStore = create<SimulationState>((set, get) => {
  const wrapApi = async (fn: () => Promise<any>, onSuccess?: (data?: any) => void) => {
    try {
      const result = await fn();
      if (result instanceof Response && !result.ok) {
        throw new Error(await result.text() || result.statusText);
      }
      if (onSuccess) onSuccess(result);
      set({ error: null });
    } catch (err) {
      set({ error: err instanceof Error ? err.message : String(err) });
    }
  };

  return {
    status: 'IDLE',
    snapshot: null,
    error: null,
    history: [],
    populationHistory: [],
    viewingHistory: false,
    connected: false,
    setSnapshot: (snapshot) => set({ snapshot }),
    setLiveSnapshot: (snapshot) => set((state) => {
      if (state.viewingHistory) return state;
      return { snapshot };
    }),
    addPopulationPoint: (point) => set((state) => {
      const newHistory = [...state.populationHistory, point].slice(-50); // Keep last 50 points
      return { populationHistory: newHistory };
    }),
    setStatus: (status) => set({ status }),
    setError: (error) => set({ error }),
    setConnected: (connected) => set({ connected }),
    exitHistoryView: () => set({ viewingHistory: false }),

    updateStatus: () => wrapApi(
      () => simulationApi.getStatus(),
      (data) => set({ status: data.status })
    ),

    start: (type, width = 20, height = 20, tickMs = 100) => wrapApi(
      () => simulationApi.start(type.toUpperCase(), width, height, tickMs),
      () => {
        set({ viewingHistory: false, populationHistory: [] });
        get().updateStatus();
      }
    ),

    startFromSnapshot: (filename, type, tickMs = 100) => wrapApi(
      () => simulationApi.startFromSnapshot(filename, type.toUpperCase(), tickMs),
      () => {
        set({ viewingHistory: false, populationHistory: [] });
        get().updateStatus();
      }
    ),

    pause: () => wrapApi(
      () => simulationApi.pause(),
      () => get().updateStatus()
    ),

    resume: () => wrapApi(
      () => simulationApi.resume(),
      () => get().updateStatus()
    ),

    stop: () => wrapApi(
      () => simulationApi.stop(),
      () => {
        get().updateStatus();
        set({ snapshot: null, viewingHistory: false, populationHistory: [] });
      }
    ),

    fetchHistory: () => wrapApi(
      () => simulationApi.getHistory(),
      (data) => set({ history: data.filenames || [] })
    ),

    saveSnapshot: () => wrapApi(
      () => simulationApi.saveSnapshot(),
      () => get().fetchHistory()
    ),

    loadHistoricalSnapshot: (filename) => wrapApi(
      () => simulationApi.getHistoricalSnapshot(filename),
      (snapshot) => set({ snapshot, viewingHistory: true })
    ),
  };
});
