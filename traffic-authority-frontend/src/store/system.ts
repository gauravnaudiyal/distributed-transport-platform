import { create } from 'zustand';
import type { HealthCheckpoint, RegionalCapacity, CongestedCorridor } from '../types/index';

interface SystemState {
    healthCheckpoints: HealthCheckpoint[];
    regionalCapacity: RegionalCapacity[];
    congestedCorridors: CongestedCorridor[];
    globalCongestion: number;
    lastUpdate: string;
}

interface SystemActions {
    setHealthCheckpoints: (checkpoints: HealthCheckpoint[]) => void;
    setRegionalCapacity: (capacity: RegionalCapacity[]) => void;
    setCongestedCorridors: (corridors: CongestedCorridor[]) => void;
    setGlobalCongestion: (percentage: number) => void;
    setLastUpdate: (timestamp: string) => void;
}

type SystemStateWithActions = SystemState & SystemActions;

export const useSystemStore = create<SystemStateWithActions>((set) => ({
    healthCheckpoints: [],
    regionalCapacity: [],
    congestedCorridors: [],
    globalCongestion: 74,
    lastUpdate: new Date().toISOString(),

    setHealthCheckpoints: (checkpoints) => set({ healthCheckpoints: checkpoints }),
    setRegionalCapacity: (capacity) => set({ regionalCapacity: capacity }),
    setCongestedCorridors: (corridors) => set({ congestedCorridors: corridors }),
    setGlobalCongestion: (percentage) => set({ globalCongestion: percentage }),
    setLastUpdate: (timestamp) => set({ lastUpdate: timestamp }),
}));
