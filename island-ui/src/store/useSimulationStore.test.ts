import { test, expect, vi, beforeEach } from 'vitest';
import { useSimulationStore } from './useSimulationStore';

// Mock fetch globally
globalThis.fetch = vi.fn();

beforeEach(() => {
  vi.resetAllMocks();
  // Reset store to initial state
  useSimulationStore.setState({ status: 'IDLE', error: null, history: [], snapshot: null });
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
