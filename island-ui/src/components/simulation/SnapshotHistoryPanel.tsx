import { useSimulationStore } from '../../store/useSimulationStore';

interface SnapshotHistoryPanelProps {
  configTickMs: number;
}

export function SnapshotHistoryPanel({ configTickMs }: SnapshotHistoryPanelProps) {
  const { 
    history, 
    viewingHistory, 
    startFromSnapshot, 
    loadHistoricalSnapshot, 
    exitHistoryView 
  } = useSimulationStore();

  return (
    <div className="panel">
      <div className="panel-header">
        <h3 className="panel-title">Snapshot History</h3>
        {viewingHistory && (
          <button
            onClick={exitHistoryView}
            className="btn btn-secondary btn-sm"
            title="Return to live updates"
          >
            Live View
          </button>
        )}
      </div>
      
      {history.length === 0 ? (
        <p className="empty-text">
          No snapshots saved yet.
        </p>
      ) : (
        <ul className="history-list margin-top-10">
          {history.map(filename => (
            <li key={filename} className="history-item">
              <button 
                onClick={() => loadHistoricalSnapshot(filename)}
                className="btn-snapshot-view"
                title="View Snapshot"
              >
                {filename.replace('.json', '')}
              </button>
              
              <button 
                onClick={() => startFromSnapshot(filename, 'nature', configTickMs)}
                className="btn btn-success btn-sm"
                title="Start Nature simulation from this snapshot"
              >
                ▶ N
              </button>
              
              <button 
                onClick={() => startFromSnapshot(filename, 'simcity', configTickMs)}
                className="btn btn-primary btn-sm"
                title="Start SimCity simulation from this snapshot"
              >
                ▶ C
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
