import { useEffect, useState } from 'react';

interface HeaderProps {
  connected: boolean;
  status: string;
}

export function Header({ connected, status }: HeaderProps) {
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    return (localStorage.getItem('theme') as 'light' | 'dark') || 'light';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  return (
    <header className="app-header">
      <h1>Island Simulator</h1>
      <div className="app-header-right">
        <button 
          className="theme-toggle" 
          onClick={toggleTheme}
          title={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
        >
          {theme === 'light' ? '🌙' : '☀️'}
        </button>
        <span className={`status-badge ${connected ? 'connected' : 'disconnected'}`}>
          {connected ? '● WebSocket Connected' : '○ Disconnected'}
        </span>
        <span className="status-text">
          Status: <span className="status-value">{status}</span>
        </span>
      </div>
    </header>
  );
}
