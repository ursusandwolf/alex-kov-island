export const API_BASE = '/api/v1/simulation';

export const simulationApi = {
  getStatus: () => fetch(`${API_BASE}/status`).then(r => r.json()),
  start: (type: string, width: number, height: number, tickMs: number) => 
    fetch(`${API_BASE}/start?type=${type}&width=${width}&height=${height}&tickMs=${tickMs}`, { method: 'POST' }),
  startFromSnapshot: (filename: string, type: string, tickMs: number) =>
    fetch(`${API_BASE}/start-from-snapshot?filename=${filename}&type=${type}&tickMs=${tickMs}`, { method: 'POST' }),
  pause: () => fetch(`${API_BASE}/pause`, { method: 'POST' }),
  resume: () => fetch(`${API_BASE}/resume`, { method: 'POST' }),
  stop: () => fetch(`${API_BASE}/stop`, { method: 'POST' }),
  getHistory: () => fetch(`${API_BASE}/snapshot/history`).then(r => r.json()),
  saveSnapshot: () => fetch(`${API_BASE}/snapshot/save`, { method: 'POST' }),
  getHistoricalSnapshot: (filename: string) => fetch(`${API_BASE}/snapshot/history/${filename}`).then(r => r.json()),
};
