import { useSimulationStore } from '../../store/useSimulationStore';

interface SimulationControlsProps {
  configWidth: number;
  configHeight: number;
  configTickMs: number;
  onConfigChange: (width: number, height: number, tickMs: number) => void;
}

export function SimulationControls({ 
  configWidth, 
  configHeight, 
  configTickMs, 
  onConfigChange 
}: SimulationControlsProps) {
  const { status, start, pause, resume, stop, saveSnapshot } = useSimulationStore();

  return (
    <div className="controls-container">
      <div className="config-group">
        <label className="config-label">
          Width: 
          <input 
            type="number" min="1" max="100" 
            value={configWidth} 
            onChange={e => onConfigChange(Number(e.target.value), configHeight, configTickMs)} 
            className="config-input" 
          />
        </label>
        <label className="config-label">
          Height: 
          <input 
            type="number" min="1" max="100" 
            value={configHeight} 
            onChange={e => onConfigChange(configWidth, Number(e.target.value), configTickMs)} 
            className="config-input" 
          />
        </label>
        <label className="config-label">
          Tick (ms): 
          <input 
            type="number" min="10" max="5000" 
            value={configTickMs} 
            onChange={e => onConfigChange(configWidth, configHeight, Number(e.target.value))} 
            className="config-input" 
          />
        </label>
      </div>
      
      <button 
        onClick={() => start('nature', configWidth, configHeight, configTickMs)} 
        className="btn btn-success"
      >
        Start Nature
      </button>
      
      <button 
        onClick={() => start('simcity', configWidth, configHeight, configTickMs)} 
        className="btn btn-primary"
      >
        Start SimCity
      </button>
      
      <button 
        onClick={pause} 
        disabled={status !== 'RUNNING'} 
        className="btn"
      >
        Pause
      </button>
      
      <button 
        onClick={resume} 
        disabled={status !== 'PAUSED'} 
        className="btn"
      >
        Resume
      </button>
      
      <button 
        onClick={stop} 
        disabled={status === 'IDLE'} 
        className="btn btn-danger"
      >
        Stop
      </button>
      
      <button 
        onClick={saveSnapshot} 
        disabled={status === 'IDLE'} 
        className="btn btn-warning ml-auto"
      >
        Save Snapshot
      </button>
    </div>
  );
}
