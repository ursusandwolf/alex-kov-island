import { useState, useMemo } from 'react';
import { useSimulationStore } from './store/useSimulationStore';
import { useSimulationSocket } from './hooks/useSimulationSocket';
import { useSimulationStatus } from './hooks/useSimulationQueries';
import WorldCanvas from './components/WorldCanvas';
import { SimulationControls } from './components/simulation/SimulationControls';
import { SimulationMetrics } from './components/simulation/SimulationMetrics';
import { SnapshotHistoryPanel } from './components/simulation/SnapshotHistoryPanel';
import { Header } from './components/layout/Header';
import { CellDetails } from './components/simulation/CellDetails';
import { Legend } from './components/simulation/Legend';
import { PopulationChart } from './components/simulation/PopulationChart';
import { ToastContainer } from './shared/ui';
import './App.css';

function App() {
  const snapshot = useSimulationStore(state => state.snapshot);
  const { data: statusData, error: statusError } = useSimulationStatus();
  const status = statusData?.status || 'IDLE';
  
  const { connected } = useSimulationSocket();
  const [selectedCoords, setSelectedCoords] = useState<string | null>(null);
  const [config, setConfig] = useState({ width: 20, height: 20, tickMs: 100 });

  const selectedNode = useMemo(() => {
    if (!snapshot || !selectedCoords) return null;
    const parts = selectedCoords.split(',');
    if (parts.length !== 2) return null;
    const x = parseInt(parts[0], 10);
    const y = parseInt(parts[1], 10);
    return snapshot.nodes[x]?.[y] ?? null;
  }, [snapshot, selectedCoords]);

  const error = statusError instanceof Error ? statusError.message : null;

  return (
    <div className="app-container">
      <ToastContainer />
      <Header connected={connected} status={status} />

      {error && (
        <div className="error-banner">
          ⚠️ Error: {error}
        </div>
      )}

      <main className="app-main">
        <section>
          <SimulationControls 
            configWidth={config.width}
            configHeight={config.height}
            configTickMs={config.tickMs}
            onConfigChange={(w, h, t) => setConfig({ width: w, height: h, tickMs: t })}
          />
          <WorldCanvas 
            snapshot={snapshot} 
            selectedCoords={selectedCoords} 
            onCellClick={setSelectedCoords} 
          />
          <PopulationChart />
        </section>

        <aside>
          <SimulationMetrics 
            tickCount={snapshot?.tickCount}
            totalEntityCount={snapshot?.totalEntityCount}
            width={snapshot?.width}
            height={snapshot?.height}
            metrics={snapshot?.metrics}
          />

          {selectedNode && <CellDetails node={selectedNode} />}

          <SnapshotHistoryPanel configTickMs={config.tickMs} />

          <Legend />
        </aside>
      </main>
    </div>
  );
}

export default App;
