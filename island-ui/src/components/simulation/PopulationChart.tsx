import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend as RechartsLegend, 
  ResponsiveContainer 
} from 'recharts';
import { useSimulationStore } from '../../store/useSimulationStore';
import { getSpeciesColor } from '../../utils/colors';
import { Panel } from '../../shared/ui';

export function PopulationChart() {
  const data = useSimulationStore(state => state.populationHistory);
  
  if (data.length === 0) {
    return (
      <Panel title="Population Dynamics">
        <div style={{ height: '250px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <p className="empty-text">No population data yet. Start simulation to see dynamics.</p>
        </div>
      </Panel>
    );
  }

  // Get all unique species keys present in the data (excluding 'tick')
  const speciesKeys = Array.from(
    new Set(data.flatMap(point => Object.keys(point).filter(k => k !== 'tick')))
  );

  return (
    <Panel title="Population Dynamics">
      <div style={{ height: '300px', paddingBottom: '20px' }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
            <XAxis 
              dataKey="tick" 
              tick={{ fontSize: 12 }} 
              label={{ value: 'Tick', position: 'insideBottomRight', offset: -5, fontSize: 12 }} 
            />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip 
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
            />
            <RechartsLegend verticalAlign="top" height={36} iconType="circle" />
            {speciesKeys.map(species => (
              <Line
                key={species}
                type="monotone"
                dataKey={species}
                stroke={getSpeciesColor(species, species === 'plant' || species === 'grass')}
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
                isAnimationActive={false}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </Panel>
  );
}
