export function Legend() {
  return (
    <div className="panel">
      <h3>Legend</h3>
      <ul className="legend-list">
        <LegendItem color="#4caf50" label="Plants / Residential" />
        <LegendItem color="#2196f3" label="Herbivores / Commercial" />
        <LegendItem color="#f44336" label="Predators / Industrial" />
        <LegendItem color="#9c27b0" label="Special / Others" />
      </ul>
    </div>
  );
}

const LegendItem = ({ color, label }: { color: string, label: string }) => (
  <li className="legend-item">
    <div className="legend-color" style={{ background: color }} />
    <span className="legend-label">{label}</span>
  </li>
);
