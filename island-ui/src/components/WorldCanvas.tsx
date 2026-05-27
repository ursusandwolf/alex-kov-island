import { useEffect, useRef, useState, useCallback } from 'react';
import { WorldSnapshot, NodeSnapshot } from '../types/simulation';
import { getSpeciesColor } from '../utils/colors';
import { Tooltip } from '../shared/ui';

interface WorldCanvasProps {
  snapshot: WorldSnapshot | null;
  cellSize?: number;
  selectedCoords?: string | null;
  onCellClick?: (coords: string | null) => void;
}

interface Transform {
  x: number;
  y: number;
  scale: number;
}

export function WorldCanvas({ 
  snapshot, 
  cellSize = 20, 
  selectedCoords, 
  onCellClick 
}: WorldCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  
  const [transform, setTransform] = useState<Transform>({ x: 20, y: 20, scale: 1 });
  const [isDragging, setIsDragging] = useState(false);
  const lastMousePos = useRef({ x: 0, y: 0 });

  // Tooltip state
  const [hoveredNode, setHoveredNode] = useState<NodeSnapshot | null>(null);
  const [mousePos, setMousePos] = useState({ x: 0, y: 0 });

  const draw = useCallback(() => {
    if (!snapshot || !canvasRef.current || !containerRef.current) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const container = containerRef.current;
    const rect = container.getBoundingClientRect();
    
    const dpr = window.devicePixelRatio || 1;
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    canvas.style.width = `${rect.width}px`;
    canvas.style.height = `${rect.height}px`;
    
    ctx.scale(dpr, dpr);
    ctx.fillStyle = '#f5f5f5';
    ctx.fillRect(0, 0, rect.width, rect.height);

    ctx.save();
    ctx.translate(transform.x, transform.y);
    ctx.scale(transform.scale, transform.scale);

    const { width, height, nodes } = snapshot;
    ctx.fillStyle = '#eeeeee';
    ctx.fillRect(0, 0, width * cellSize, height * cellSize);

    for (let x = 0; x < width; x++) {
      for (let y = 0; y < height; y++) {
        const node = nodes[x][y];
        if (!node) continue;
        
        const color = getSpeciesColor(node.topSpeciesCode, node.topSpeciesPlant);
        ctx.fillStyle = color;
        ctx.fillRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);

        if (selectedCoords && node.coordinates === selectedCoords) {
          ctx.strokeStyle = '#ffeb3b';
          ctx.lineWidth = 3 / transform.scale;
          ctx.strokeRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);
        }
      }
    }
    
    ctx.restore();
  }, [snapshot, cellSize, selectedCoords, transform]);

  useEffect(() => {
    draw();
  }, [draw]);

  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    const factor = Math.pow(1.1, (-e.deltaY) / 100);
    const newScale = Math.min(Math.max(transform.scale * factor, 0.1), 5);
    
    if (newScale === transform.scale) return;

    const rect = canvasRef.current!.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const newX = mouseX - (mouseX - transform.x) * (newScale / transform.scale);
    const newY = mouseY - (mouseY - transform.y) * (newScale / transform.scale);

    setTransform({ x: newX, y: newY, scale: newScale });
  };

  const handleMouseDown = (e: React.MouseEvent) => {
    if (e.button !== 0) return;
    setIsDragging(true);
    lastMousePos.current = { x: e.clientX, y: e.clientY };
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    setMousePos({ x: e.clientX, y: e.clientY });

    if (isDragging) {
      const dx = e.clientX - lastMousePos.current.x;
      const dy = e.clientY - lastMousePos.current.y;
      setTransform(prev => ({ ...prev, x: prev.x + dx, y: prev.y + dy }));
      lastMousePos.current = { x: e.clientX, y: e.clientY };
    }

    // Update hovered node
    if (!snapshot || !canvasRef.current) return;
    const rect = canvasRef.current.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;
    
    const worldX = (clickX - transform.x) / transform.scale;
    const worldY = (clickY - transform.y) / transform.scale;
    
    const gridX = Math.floor(worldX / cellSize);
    const gridY = Math.floor(worldY / cellSize);
    
    if (gridX >= 0 && gridX < snapshot.width && gridY >= 0 && gridY < snapshot.height) {
      setHoveredNode(snapshot.nodes[gridX][gridY]);
    } else {
      setHoveredNode(null);
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleClick = (e: React.MouseEvent) => {
    if (!snapshot || !onCellClick || !canvasRef.current) return;
    
    const rect = canvasRef.current.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;
    
    const worldX = (clickX - transform.x) / transform.scale;
    const worldY = (clickY - transform.y) / transform.scale;
    
    const gridX = Math.floor(worldX / cellSize);
    const gridY = Math.floor(worldY / cellSize);
    
    if (gridX >= 0 && gridX < snapshot.width && gridY >= 0 && gridY < snapshot.height) {
      const node = snapshot.nodes[gridX][gridY];
      if (selectedCoords === node.coordinates) {
        onCellClick(null);
      } else {
        onCellClick(node.coordinates);
      }
    }
  };

  const resetTransform = () => {
    setTransform({ x: 20, y: 20, scale: 1 });
  };

  if (!snapshot) {
    return (
      <div className="waiting-container">
        Waiting for simulation data...
      </div>
    );
  }

  return (
    <div className="canvas-wrapper">
      <div className="canvas-toolbar">
        <span className="zoom-info">Zoom: {Math.round(transform.scale * 100)}%</span>
        <button className="btn btn-sm btn-secondary" onClick={resetTransform}>Reset View</button>
      </div>
      <div 
        ref={containerRef}
        className="canvas-container" 
        style={{ height: '500px', overflow: 'hidden', cursor: isDragging ? 'grabbing' : 'grab', position: 'relative' }}
        onWheel={handleWheel}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={() => { handleMouseUp(); setHoveredNode(null); }}
      >
        <canvas 
          ref={canvasRef} 
          className="world-canvas"
          onClick={handleClick}
        />
        
        <Tooltip x={mousePos.x} y={mousePos.y} visible={!!hoveredNode && !isDragging}>
          {hoveredNode && (
            <div className="tooltip-content">
              <div className="tooltip-coords">{hoveredNode.coordinates}</div>
              <div className="tooltip-species">
                {hoveredNode.topSpeciesCode ? (
                  <>
                    <span 
                      className="species-dot" 
                      style={{ backgroundColor: getSpeciesColor(hoveredNode.topSpeciesCode, hoveredNode.topSpeciesPlant) }}
                    />
                    {hoveredNode.topSpeciesCode}
                  </>
                ) : (
                  'Empty Cell'
                )}
              </div>
              {hoveredNode.hasOrganisms && (
                <div className="tooltip-extra">Contains organisms</div>
              )}
            </div>
          )}
        </Tooltip>
      </div>
      <div className="canvas-hint">
        💡 Use <b>Mouse Wheel</b> to zoom, <b>Click & Drag</b> to pan. <b>Hover</b> to see details.
      </div>
    </div>
  );
}

export default WorldCanvas;
