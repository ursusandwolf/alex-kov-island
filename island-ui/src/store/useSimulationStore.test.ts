import { test, expect, vi, beforeEach } from 'vitest';
import { useSimulationStore } from './useSimulationStore';

// Mock fetch globally
globalThis.fetch = vi.fn();

beforeEach(() => {
  vi.resetAllMocks();
  // Reset store to initial state
  useSimulationStore.setState({ status: 'IDLE', error: null, history: [], snapshot: null, viewingHistory: false });
});

test('updateStatus success updates the status', async () => {
  vi.mocked(globalThis.fetch).mockResolvedValueOnce({
    ok: true,
    json: async () => ({ status: 'RUNNING' }),
  } as Response);

  await useSimulationStore.getState().updateStatus();

  expect(globalThis.fetch).toHaveBeenCalledWith('/api/v1/simulation/status');
  expect(useSimulationStore.getState().status).toBe('RUNNING');
  expect(useSimulationStore.getState().error).toBeNull();
});

test('pause success updates status', async () => {
  // First mock the pause call
  vi.mocked(globalThis.fetch).mockResolvedValueOnce({
    ok: true,
    statusText: 'OK'
  } as Response);
  
  // Then mock the subsequent updateStatus call
  vi.mocked(globalThis.fetch).mockResolvedValueOnce({
    ok: true,
    json: async () => ({ status: 'PAUSED' }),
  } as Response);

  await useSimulationStore.getState().pause();

  expect(globalThis.fetch).toHaveBeenCalledWith('/api/v1/simulation/pause', { method: 'POST' });
  expect(useSimulationStore.getState().status).toBe('PAUSED');
  expect(useSimulationStore.getState().error).toBeNull();
});

test('live snapshots do not overwrite history view until user exits it', async () => {
  useSimulationStore.setState({
    snapshot: {
      tickCount: 10,
      width: 1,
      height: 1,
      totalEntityCount: 5,
      metrics: {},
      nodes: [],
    },
    viewingHistory: true,
  });

  useSimulationStore.getState().setLiveSnapshot({
    tickCount: 11,
    width: 1,
    height: 1,
    totalEntityCount: 6,
    metrics: {},
    nodes: [],
  });

  expect(useSimulationStore.getState().snapshot?.tickCount).toBe(10);

  useSimulationStore.getState().exitHistoryView();
  useSimulationStore.getState().setLiveSnapshot({
    tickCount: 12,
    width: 1,
    height: 1,
    totalEntityCount: 7,
    metrics: {},
    nodes: [],
  });

  expect(useSimulationStore.getState().snapshot?.tickCount).toBe(12);
});
