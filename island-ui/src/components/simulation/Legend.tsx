import { getSpeciesColor } from '../../utils/colors';
import { Panel } from '../../shared/ui';

export function Legend() {
  const species = [
    { code: 'WOLF', label: 'Wolf (Predator)', plant: false },
    { code: 'RABBIT', label: 'Rabbit (Prey)', plant: false },
    { code: 'PLANT', label: 'Plant (Food)', plant: true },
    { code: 'HOUSE', label: 'House (Residential)', plant: false },
    { code: 'ROAD', label: 'Road (Infrastructure)', plant: false },
  ];

  return (
    <Panel title="Legend">
      <ul className="legend-list">
        {species.map(s => (
          <li key={s.code} className="legend-item">
            <div 
              className="legend-color" 
              style={{ backgroundColor: getSpeciesColor(s.code, s.plant) }}
            />
            <span className="legend-label">{s.label}</span>
          </li>
        ))}
      </ul>
    </Panel>
  );
}
