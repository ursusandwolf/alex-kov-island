import { useSimulationMutations, useSimulationStatus } from '../../hooks/useSimulationQueries';
import { Button, Input } from '../../shared/ui';

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
  const { data: statusData } = useSimulationStatus();
  const status = statusData?.status || 'IDLE';
  
  const { start, pause, resume, stop, save } = useSimulationMutations();

  return (
    <div className="controls-container">
      <div className="config-group">
        <Input 
          label="Width:"
          type="number" min="1" max="100" 
          value={configWidth} 
          onChange={e => onConfigChange(Number(e.target.value), configHeight, configTickMs)} 
        />
        <Input 
          label="Height:"
          type="number" min="1" max="100" 
          value={configHeight} 
          onChange={e => onConfigChange(configWidth, Number(e.target.value), configTickMs)} 
        />
        <Input 
          label="Tick (ms):"
          type="number" min="10" max="5000" 
          value={configTickMs} 
          onChange={e => onConfigChange(configWidth, configHeight, Number(e.target.value))} 
        />
      </div>
      
      <Button 
        variant="success"
        onClick={() => start.mutate({ type: 'nature', width: configWidth, height: configHeight, tickMs: configTickMs })} 
        isLoading={start.isPending}
      >
        Start Nature
      </Button>
      
      <Button 
        variant="primary"
        onClick={() => start.mutate({ type: 'simcity', width: configWidth, height: configHeight, tickMs: configTickMs })} 
        isLoading={start.isPending}
      >
        Start SimCity
      </Button>
      
      <Button 
        onClick={() => pause.mutate()} 
        disabled={status !== 'RUNNING'}
        isLoading={pause.isPending}
      >
        Pause
      </Button>
      
      <Button 
        onClick={() => resume.mutate()} 
        disabled={status !== 'PAUSED'}
        isLoading={resume.isPending}
      >
        Resume
      </Button>
      
      <Button 
        variant="danger"
        onClick={() => stop.mutate()} 
        disabled={status === 'IDLE'}
        isLoading={stop.isPending}
      >
        Stop
      </Button>
      
      <Button 
        variant="warning"
        onClick={() => save.mutate()} 
        disabled={status === 'IDLE'}
        isLoading={save.isPending}
        className="ml-auto"
      >
        Save Snapshot
      </Button>
    </div>
  );
}
