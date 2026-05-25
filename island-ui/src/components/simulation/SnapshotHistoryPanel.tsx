import React from 'react';
import { useSimulationStore } from '../../store/useSimulationStore';

export const SnapshotHistoryPanel: React.FC<{ configTickMs: number }> = ({ configTickMs }) => {
  const { history, viewingHistory, startFromSnapshot, loadHistoricalSnapshot, exitHistoryView } = useSimulationStore();

  return (
    <div style={panelStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '8px' }}>
        <h3 style={{ margin: 0 }}>Snapshot History</h3>
        {viewingHistory && (
          <button
            onClick={exitHistoryView}
            style={{
              background: '#eceff1',
              border: '1px solid #cfd8dc',
              padding: '5px 10px',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '0.8rem'
            }}
            title="Return to live updates"
          >
            Live View
          </button>
        )}
      </div>
      {history.length === 0 ? (
        <p style={{ color: '#888', fontSize: '0.9rem' }}>No snapshots saved yet.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, maxHeight: '200px', overflowY: 'auto' }}>
          {history.map(filename => (
            <li key={filename} style={{ marginBottom: '8px', display: 'flex', gap: '5px' }}>
              <button 
                onClick={() => loadHistoricalSnapshot(filename)}
                style={{ 
                  background: 'none', 
                  border: '1px solid #ddd', 
                  padding: '5px', 
                  borderRadius: '4px', 
                  cursor: 'pointer', 
                  flexGrow: 1, 
                  textAlign: 'left',
                  fontSize: '0.8rem'
                }}
                title="View Snapshot"
              >
                {filename.replace('.json', '')}
              </button>
              <button 
                onClick={() => startFromSnapshot(filename, 'nature', configTickMs)}
                style={{ 
                  background: '#4caf50', 
                  color: 'white',
                  border: 'none', 
                  padding: '5px 10px', 
                  borderRadius: '4px', 
                  cursor: 'pointer',
                  fontSize: '0.8rem'
                }}
                title="Start Nature simulation from this snapshot"
              >
                ▶ Nature
              </button>
              <button 
                onClick={() => startFromSnapshot(filename, 'simcity', configTickMs)}
                style={{ 
                  background: '#2196f3', 
                  color: 'white',
                  border: 'none', 
                  padding: '5px 10px', 
                  borderRadius: '4px', 
                  cursor: 'pointer',
                  fontSize: '0.8rem'
                }}
                title="Start SimCity simulation from this snapshot"
              >
                ▶ City
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

const panelStyle: React.CSSProperties = {
  background: 'white',
  padding: '15px',
  borderRadius: '8px',
  marginBottom: '20px',
  boxShadow: '0 2px 5px rgba(0,0,0,0.05)',
};
