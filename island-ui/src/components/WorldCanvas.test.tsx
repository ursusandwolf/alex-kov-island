import { render, screen, fireEvent } from '@testing-library/react';
import WorldCanvas from './WorldCanvas';
import { expect, test, vi } from 'vitest';

test('renders waiting message when snapshot is null', () => {
  render(<WorldCanvas snapshot={null} />);
  expect(screen.getByText(/Waiting for simulation data/i)).toBeInTheDocument();
});

test('handles cell click', () => {
  const mockSnapshot = {
    width: 2,
    height: 2,
    tickCount: 1,
    totalEntityCount: 0,
    metrics: {},
    nodes: [
      [
        { coordinates: '0,0', topSpeciesCode: null, topSpeciesPlant: false, hasOrganisms: false, entityCounts: {} },
        { coordinates: '0,1', topSpeciesCode: null, topSpeciesPlant: false, hasOrganisms: false, entityCounts: {} }
      ],
      [
        { coordinates: '1,0', topSpeciesCode: null, topSpeciesPlant: false, hasOrganisms: false, entityCounts: {} },
        { coordinates: '1,1', topSpeciesCode: null, topSpeciesPlant: false, hasOrganisms: false, entityCounts: {} }
      ]
    ]
  };

  const onCellClick = vi.fn();
  // Using cellSize=10 for simplicity in test
  const { container } = render(
    <WorldCanvas snapshot={mockSnapshot as any} cellSize={10} onCellClick={onCellClick} />
  );

  const canvas = container.querySelector('canvas');
  expect(canvas).toBeInTheDocument();

  if (canvas) {
    // The component defaults to transform {x: 20, y: 20, scale: 1}
    // Cell (1,1) at world coords (10, 10)
    // Canvas coords = transform.x + worldX * scale = 20 + 10 * 1 = 30
    // Canvas coords = transform.y + worldY * scale = 20 + 10 * 1 = 30
    
    canvas.getBoundingClientRect = vi.fn(() => ({
      left: 0,
      top: 0,
      width: 100,
      height: 100,
      right: 100,
      bottom: 100,
      x: 0,
      y: 0,
      toJSON: () => {}
    }));
    
    // Click at (35, 35) to be safely inside cell (1,1) which starts at (30,30) in canvas space
    fireEvent.click(canvas, { clientX: 35, clientY: 35 });
  }

  expect(onCellClick).toHaveBeenCalledWith('1,1');
});
