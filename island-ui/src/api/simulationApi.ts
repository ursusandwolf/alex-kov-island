import { WorldSnapshot, SimulationStatus } from '../types/simulation';

export const API_BASE = '/api/v1/simulation';

export interface StatusResponse {
  status: SimulationStatus;
}

export interface HistoryResponse {
  filenames: string[];
}

const readText = async (response: Response): Promise<string> => {
  if (!response.ok) {
    throw new Error(await response.text() || response.statusText);
  }
  return response.text();
};

const readJson = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    throw new Error(await response.text() || response.statusText);
  }
  return response.json() as Promise<T>;
};

export const simulationApi = {
  getStatus: (): Promise<StatusResponse> => fetch(`${API_BASE}/status`).then(response => readJson<StatusResponse>(response)),
  start: (type: string, width: number, height: number, tickMs: number): Promise<StatusResponse> =>
    fetch(`${API_BASE}/start?type=${type}&width=${width}&height=${height}&tickMs=${tickMs}`, { method: 'POST' })
      .then(response => readJson<StatusResponse>(response)),
  startFromSnapshot: (filename: string, type: string, tickMs: number): Promise<StatusResponse> =>
    fetch(`${API_BASE}/start-from-snapshot?filename=${filename}&type=${type}&tickMs=${tickMs}`, { method: 'POST' })
      .then(response => readJson<StatusResponse>(response)),
  pause: (): Promise<StatusResponse> =>
    fetch(`${API_BASE}/pause`, { method: 'POST' }).then(response => readJson<StatusResponse>(response)),
  resume: (): Promise<StatusResponse> =>
    fetch(`${API_BASE}/resume`, { method: 'POST' }).then(response => readJson<StatusResponse>(response)),
  stop: (): Promise<StatusResponse> =>
    fetch(`${API_BASE}/stop`, { method: 'POST' }).then(response => readJson<StatusResponse>(response)),
  getHistory: (): Promise<HistoryResponse> => fetch(`${API_BASE}/snapshot/history`).then(response => readJson<HistoryResponse>(response)),
  saveSnapshot: (): Promise<string> =>
    fetch(`${API_BASE}/snapshot/save`, { method: 'POST' }).then(response => readText(response)),
  getHistoricalSnapshot: (filename: string): Promise<WorldSnapshot> => fetch(`${API_BASE}/snapshot/history/${filename}`).then(response => readJson<WorldSnapshot>(response)),
};
