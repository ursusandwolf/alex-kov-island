import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { simulationApi } from '../api/simulationApi';

export const useSimulationStatus = () => {
  return useQuery({
    queryKey: ['simulation', 'status'],
    queryFn: simulationApi.getStatus,
    refetchInterval: 2000,
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

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['simulation'] });

  const start = useMutation({ mutationFn: (vars: any) => simulationApi.start(vars.type, vars.width, vars.height, vars.tickMs), onSuccess: invalidate });
  const stop = useMutation({ mutationFn: simulationApi.stop, onSuccess: invalidate });
  const pause = useMutation({ mutationFn: simulationApi.pause, onSuccess: invalidate });
  const resume = useMutation({ mutationFn: simulationApi.resume, onSuccess: invalidate });
  const save = useMutation({ mutationFn: simulationApi.saveSnapshot, onSuccess: () => queryClient.invalidateQueries({ queryKey: ['simulation', 'history'] }) });

  return { start, stop, pause, resume, save };
};
