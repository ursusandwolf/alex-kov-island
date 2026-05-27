import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
}

export const Input: React.FC<InputProps> = ({
  label,
  className = '',
  id,
  ...props
}) => {
  return (
    <div className="input-wrapper">
      {label && (
        <label htmlFor={id} className="config-label">
          {label}
        </label>
      )}
      <input
        id={id}
        className={`config-input ${className}`}
        {...props}
      />
    </div>
  );
};
