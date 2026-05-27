import { test, expect, beforeEach } from 'vitest';
import { useSimulationStore } from './useSimulationStore';

beforeEach(() => {
  // Reset store to initial state
  useSimulationStore.setState({ 
    snapshot: null, 
    populationHistory: [], 
    viewingHistory: false, 
    connected: false 
  });
});

test('setSnapshot updates the snapshot', () => {
  const mockSnapshot = { tickCount: 5 } as any;
  useSimulationStore.getState().setSnapshot(mockSnapshot);
  expect(useSimulationStore.getState().snapshot?.tickCount).toBe(5);
});

test('live snapshots do not overwrite history view until user exits it', () => {
  useSimulationStore.setState({
    snapshot: { tickCount: 10 } as any,
    viewingHistory: true,
  });

  useSimulationStore.getState().setLiveSnapshot({ tickCount: 11 } as any);
  expect(useSimulationStore.getState().snapshot?.tickCount).toBe(10);

  useSimulationStore.getState().exitHistoryView();
  useSimulationStore.getState().setLiveSnapshot({ tickCount: 12 } as any);
  expect(useSimulationStore.getState().snapshot?.tickCount).toBe(12);
});

test('resetPopulationHistory clears the history', () => {
  useSimulationStore.setState({ populationHistory: [{ tick: 1 }] });
  useSimulationStore.getState().resetPopulationHistory();
  expect(useSimulationStore.getState().populationHistory).toHaveLength(0);
});
