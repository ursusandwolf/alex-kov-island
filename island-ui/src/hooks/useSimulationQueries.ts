import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { simulationApi } from '../api/simulationApi';
import { useSimulationStore } from '../store/useSimulationStore';
import { useToast } from '../store/useToastStore';

export const useSimulationStatus = () => {
  const connected = useSimulationStore(state => state.connected);

  return useQuery({
    queryKey: ['simulation', 'status'],
    queryFn: simulationApi.getStatus,
    refetchInterval: connected ? false : 3000,
  });
};

export const useSimulationHistory = () => {
  return useQuery({
    queryKey: ['simulation', 'history'],
    queryFn: simulationApi.getHistory,
  });
};

export const useSimulationMutations = () => {
  const queryClient = useQueryClient();
  const setSnapshot = useSimulationStore(state => state.setSnapshot);
  const setViewingHistory = useSimulationStore(state => state.setViewingHistory);
  const resetPopulationHistory = useSimulationStore(state => state.resetPopulationHistory);
  const toast = useToast();

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['simulation'] });

  const start = useMutation({ 
    mutationFn: (vars: { type: string, width: number, height: number, tickMs: number }) => 
      simulationApi.start(vars.type, vars.width, vars.height, vars.tickMs), 
    onSuccess: (_, vars) => {
      resetPopulationHistory();
      setViewingHistory(false);
      invalidate();
      toast.success(`Simulation (${vars.type}) started successfully`);
    },
    onError: (err: any) => toast.error(`Failed to start simulation: ${err.message}`)
  });

  const startFromSnapshot = useMutation({
    mutationFn: (vars: { filename: string, type: string, tickMs: number }) =>
      simulationApi.startFromSnapshot(vars.filename, vars.type, vars.tickMs),
    onSuccess: (_, vars) => {
      resetPopulationHistory();
      setViewingHistory(false);
      invalidate();
      toast.success(`Simulation started from ${vars.filename}`);
    },
    onError: (err: any) => toast.error(`Failed to start from snapshot: ${err.message}`)
  });

  const stop = useMutation({ 
    mutationFn: simulationApi.stop, 
    onSuccess: () => {
      setSnapshot(null);
      setViewingHistory(false);
      resetPopulationHistory();
      invalidate();
      toast.info('Simulation stopped');
    },
    onError: (err: any) => toast.error(`Failed to stop: ${err.message}`)
  });

  const pause = useMutation({ 
    mutationFn: simulationApi.pause, 
    onSuccess: () => {
      invalidate();
      toast.info('Simulation paused');
    }
  });

  const resume = useMutation({ 
    mutationFn: simulationApi.resume, 
    onSuccess: () => {
      invalidate();
      toast.success('Simulation resumed');
    }
  });
  
  const save = useMutation({ 
    mutationFn: simulationApi.saveSnapshot, 
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['simulation', 'history'] });
      toast.success('Snapshot saved successfully');
    },
    onError: (err: any) => toast.error(`Failed to save snapshot: ${err.message}`)
  });

  const loadHistoricalSnapshot = useMutation({
    mutationFn: (filename: string) => simulationApi.getHistoricalSnapshot(filename),
    onSuccess: (snapshot, filename) => {
      setSnapshot(snapshot);
      setViewingHistory(true);
      toast.info(`Viewing snapshot: ${filename}`);
    },
    onError: (err: any) => toast.error(`Failed to load snapshot: ${err.message}`)
  });

  return { 
    start, 
    startFromSnapshot,
    stop, 
    pause, 
    resume, 
    save,
    loadHistoricalSnapshot
  };
};
