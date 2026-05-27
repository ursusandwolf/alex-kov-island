import { NodeSnapshot } from '../../types/simulation';
import { Panel } from '../../shared/ui';

interface CellDetailsProps {
  node: NodeSnapshot;
}

export function CellDetails({ node }: CellDetailsProps) {
  return (
    <Panel title="Cell Details" variant="details">
      <p><strong>Coordinates:</strong> {node.coordinates}</p>
      <p><strong>Top Species:</strong> {node.topSpeciesCode || 'None'}</p>
      <p><strong>Is Plant:</strong> {node.topSpeciesPlant ? 'Yes' : 'No'}</p>
      <p><strong>Has Organisms:</strong> {node.hasOrganisms ? 'Yes' : 'No'}</p>
    </Panel>
  );
}
