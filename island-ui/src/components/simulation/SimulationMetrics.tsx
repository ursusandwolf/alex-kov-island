interface SimulationMetricsProps {
  tickCount?: number;
  totalEntityCount?: number;
  width?: number;
  height?: number;
  metrics?: Record<string, string | number>;
}

export function SimulationMetrics({ 
  tickCount, 
  totalEntityCount, 
  width, 
  height, 
  metrics 
}: SimulationMetricsProps) {
  return (
    <>
      <div className="panel">
        <h3>Simulation Info</h3>
        <p><strong>Tick:</strong> {tickCount || 0}</p>
        <p><strong>Entities:</strong> {totalEntityCount || 0}</p>
        <p><strong>Dimensions:</strong> {width !== undefined && height !== undefined ? `${width}x${height}` : 'N/A'}</p>
      </div>

      <div className="panel">
        <h3>Metrics</h3>
        {metrics ? Object.entries(metrics).map(([key, value]) => (
          <div key={key} className="metrics-row">
            <span className="metrics-key">{key}:</span>
            <span className="metrics-value">
              {typeof value === 'number' ? value.toLocaleString() : value}
            </span>
          </div>
        )) : <p>No metrics available</p>}
      </div>
    </>
  );
}
