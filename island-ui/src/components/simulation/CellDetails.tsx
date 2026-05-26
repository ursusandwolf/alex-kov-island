import { NodeSnapshot } from '../../types/simulation';

interface CellDetailsProps {
  node: NodeSnapshot;
}

export function CellDetails({ node }: CellDetailsProps) {
  return (
    <div className="panel details-panel">
      <h3>Cell Details</h3>
      <p><strong>Coordinates:</strong> {node.coordinates}</p>
      <p><strong>Top Species:</strong> {node.topSpeciesCode || 'None'}</p>
      <p><strong>Is Plant:</strong> {node.topSpeciesPlant ? 'Yes' : 'No'}</p>
      <p><strong>Has Organisms:</strong> {node.hasOrganisms ? 'Yes' : 'No'}</p>
    </div>
  );
}
