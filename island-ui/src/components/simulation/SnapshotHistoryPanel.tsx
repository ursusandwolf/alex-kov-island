import { useSimulationStore } from '../../store/useSimulationStore';
import { useSimulationMutations, useSimulationHistory } from '../../hooks/useSimulationQueries';
import { Button, Panel } from '../../shared/ui';

interface SnapshotHistoryPanelProps {
  configTickMs: number;
}

export function SnapshotHistoryPanel({ configTickMs }: SnapshotHistoryPanelProps) {
  const { data: historyData } = useSimulationHistory();
  const history = historyData?.filenames || [];
  
  const viewingHistory = useSimulationStore(state => state.viewingHistory);
  const exitHistoryView = useSimulationStore(state => state.exitHistoryView);

  const { startFromSnapshot, loadHistoricalSnapshot } = useSimulationMutations();

  const headerAction = viewingHistory && (
    <Button
      variant="secondary"
      size="sm"
      onClick={exitHistoryView}
      title="Return to live updates"
    >
      Live View
    </Button>
  );

  return (
    <Panel title="Snapshot History" headerAction={headerAction}>
      {history.length === 0 ? (
        <p className="empty-text">
          No snapshots saved yet.
        </p>
      ) : (
        <ul className="history-list margin-top-10">
          {history.map(filename => (
            <li key={filename} className="history-item">
              <button 
                onClick={() => loadHistoricalSnapshot.mutate(filename)}
                className="btn-snapshot-view"
                title="View Snapshot"
                disabled={loadHistoricalSnapshot.isPending}
              >
                {filename.replace('.json', '')}
              </button>
              
              <Button 
                variant="success"
                size="sm"
                onClick={() => startFromSnapshot.mutate({ filename, type: 'nature', tickMs: configTickMs })}
                title="Start Nature simulation from this snapshot"
                disabled={startFromSnapshot.isPending}
              >
                ▶ N
              </Button>
              
              <Button 
                variant="primary"
                size="sm"
                onClick={() => startFromSnapshot.mutate({ filename, type: 'simcity', tickMs: configTickMs })}
                title="Start SimCity simulation from this snapshot"
                disabled={startFromSnapshot.isPending}
              >
                ▶ C
              </Button>
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
}
