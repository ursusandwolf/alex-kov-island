interface HeaderProps {
  connected: boolean;
  status: string;
}

export function Header({ connected, status }: HeaderProps) {
  return (
    <header className="app-header">
      <h1>Island Simulator</h1>
      <div className="app-header-right">
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
