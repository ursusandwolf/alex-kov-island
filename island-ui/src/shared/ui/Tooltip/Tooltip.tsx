import React from 'react';

interface TooltipProps {
  x: number;
  y: number;
  visible: boolean;
  children: React.ReactNode;
}

export const Tooltip: React.FC<TooltipProps> = ({ x, y, visible, children }) => {
  if (!visible) return null;

  return (
    <div 
      className="canvas-tooltip"
      style={{ 
        left: x + 15, 
        top: y + 15,
      }}
    >
      {children}
    </div>
  );
};
