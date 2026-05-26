import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WorldSnapshot } from '../types/simulation';
import { useSimulationStore } from '../store/useSimulationStore';

export function useSimulationSocket() {
  const connected = useSimulationStore(state => state.connected);
  const setConnected = useSimulationStore(state => state.setConnected);
  const setLiveSnapshot = useSimulationStore(state => state.setLiveSnapshot);

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
        setLiveSnapshot(snapshot);
      });
    };

    client.onWebSocketClose = () => setConnected(false);

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [setLiveSnapshot, setConnected]);

  return { connected };
}
