import { useEffect, useState, useMemo } from 'react';
import { useSimulationStore } from './store/useSimulationStore';
import { useSimulationSocket } from './hooks/useSimulationSocket';
import WorldCanvas from './components/WorldCanvas';
import { SimulationControls } from './components/simulation/SimulationControls';
import { SimulationMetrics } from './components/simulation/SimulationMetrics';
import { SnapshotHistoryPanel } from './components/simulation/SnapshotHistoryPanel';
import { Header } from './components/layout/Header';
import { CellDetails } from './components/simulation/CellDetails';
import { Legend } from './components/simulation/Legend';
import { PopulationChart } from './components/simulation/PopulationChart';
import './App.css';

function App() {
  const { status, snapshot, error, updateStatus, fetchHistory } = useSimulationStore();
  const { connected } = useSimulationSocket();
  const [selectedCoords, setSelectedCoords] = useState<string | null>(null);
  const [config, setConfig] = useState({ width: 20, height: 20, tickMs: 100 });

  useEffect(() => {
    updateStatus();
    fetchHistory();
  }, [updateStatus, fetchHistory]);

  const selectedNode = useMemo(() => {
    if (!snapshot || !selectedCoords) return null;
    const [sx, sy] = selectedCoords.split(',').map(Number);
    return snapshot.nodes[sx]?.[sy] ?? null;
  }, [snapshot, selectedCoords]);

  return (
    <div className="app-container">
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
