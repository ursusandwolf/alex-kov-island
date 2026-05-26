import { useEffect, useRef } from 'react';
import { WorldSnapshot } from '../types/simulation';
import { getSpeciesColor } from '../utils/colors';

interface WorldCanvasProps {
  snapshot: WorldSnapshot | null;
  cellSize?: number;
  selectedCoords?: string | null;
  onCellClick?: (coords: string | null) => void;
}

export function WorldCanvas({ 
  snapshot, 
  cellSize = 12, 
  selectedCoords, 
  onCellClick 
}: WorldCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!snapshot || !canvasRef.current) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const { width, height, nodes } = snapshot;
    canvas.width = width * cellSize;
    canvas.height = height * cellSize;

    // Clear background
    ctx.fillStyle = '#eeeeee';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Draw nodes
    for (let x = 0; x < width; x++) {
      for (let y = 0; y < height; y++) {
        const node = nodes[x][y];
        const color = getSpeciesColor(node.topSpeciesCode, node.topSpeciesPlant);
        
        ctx.fillStyle = color;
        // Draw cell with a small gap for grid effect
        ctx.fillRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);

        // Highlight selected cell
        if (selectedCoords && node.coordinates === selectedCoords) {
          ctx.strokeStyle = '#ffeb3b'; // Yellow highlight
          ctx.lineWidth = 2;
          ctx.strokeRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);
        }
      }
    }
  }, [snapshot, cellSize, selectedCoords]);

  const handleClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!snapshot || !onCellClick || !canvasRef.current) return;
    const rect = canvasRef.current.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;
    const gridX = Math.floor(clickX / cellSize);
    const gridY = Math.floor(clickY / cellSize);
    
    if (gridX >= 0 && gridX < snapshot.width && gridY >= 0 && gridY < snapshot.height) {
      const node = snapshot.nodes[gridX][gridY];
      if (selectedCoords === node.coordinates) {
        onCellClick(null); // deselect
      } else {
        onCellClick(node.coordinates);
      }
    }
  };

  if (!snapshot) {
    return (
      <div className="waiting-container">
        Waiting for simulation data...
      </div>
    );
  }

  return (
    <div className="canvas-container">
      <canvas 
        ref={canvasRef} 
        className={`world-canvas ${onCellClick ? 'cursor-pointer' : ''}`}
        onClick={handleClick}
      />
    </div>
  );
}

export default WorldCanvas;
