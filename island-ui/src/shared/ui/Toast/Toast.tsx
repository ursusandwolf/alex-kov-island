import React from 'react';
import { Toast as ToastType, useToastStore } from '../../../store/useToastStore';

interface ToastProps {
  toast: ToastType;
}

export const Toast: React.FC<ToastProps> = ({ toast }) => {
  const removeToast = useToastStore((state) => state.removeToast);

  const getIcon = () => {
    switch (toast.type) {
      case 'success': return '✅';
      case 'error': return '❌';
      case 'warning': return '⚠️';
      case 'info': return 'ℹ️';
      default: return '●';
    }
  };

  return (
    <div className={`toast toast-${toast.type}`} onClick={() => removeToast(toast.id)}>
      <span className="toast-icon">{getIcon()}</span>
      <span className="toast-message">{toast.message}</span>
      <button className="toast-close">×</button>
    </div>
  );
};
