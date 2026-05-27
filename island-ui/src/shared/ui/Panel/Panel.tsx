import React from 'react';

interface PanelProps {
  children: React.ReactNode;
  title?: string;
  headerAction?: React.ReactNode;
  className?: string;
  variant?: 'default' | 'details';
}

export const Panel: React.FC<PanelProps> = ({
  children,
  title,
  headerAction,
  className = '',
  variant = 'default',
}) => {
  const variantClass = variant === 'details' ? 'details-panel' : '';

  return (
    <div className={`panel ${variantClass} ${className}`}>
      {(title || headerAction) && (
        <div className="panel-header">
          {title && <h3 className="panel-title">{title}</h3>}
          {headerAction && <div className="panel-action">{headerAction}</div>}
        </div>
      )}
      <div className="panel-content">
        {children}
      </div>
    </div>
  );
};
