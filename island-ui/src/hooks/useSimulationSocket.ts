import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WorldSnapshot } from '../types/simulation';
import { useSimulationStore } from '../store/useSimulationStore';

export function useSimulationSocket() {
  const [connected, setConnected] = useState(false);
  const setSnapshot = useSimulationStore(state => state.setSnapshot);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws-simulation'),
      debug: (str) => console.log('STOMP: ' + str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setConnected(true);
      client.subscribe('/topic/world-state', (message) => {
        const snapshot: WorldSnapshot = JSON.parse(message.body);
        setSnapshot(snapshot);
      });
    };

    client.onWebSocketClose = () => setConnected(false);

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [setSnapshot]);

  return { connected };
}
