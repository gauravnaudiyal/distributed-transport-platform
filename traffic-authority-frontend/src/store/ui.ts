import { create } from 'zustand';
import type { FailureSimulatorState } from '../types/index';

interface UIState {
    activeNav:
    | 'command-center'
    | 'request-passage'
    | 'my-permits'
    | 'traffic-map'
    | 'enforcement'
    | 'profile-notifications';
    failures: FailureSimulatorState;
    expandedPermitId: string | null;
    formStep: 1 | 2 | 3 | 4;
}

interface UIActions {
    setActiveNav: (nav: UIState['activeNav']) => void;
    setFailureSimulator: (key: keyof FailureSimulatorState, value: boolean) => void;
    setExpandedPermitId: (id: string | null) => void;
    setFormStep: (step: UIState['formStep']) => void;
}

type UIStateWithActions = UIState & UIActions;

export const useUIStore = create<UIStateWithActions>((set) => ({
    activeNav: 'command-center',
    failures: {
        dropNotifications: false,
        euWestDown: true,
        injectLatency: false,
        apSPartition: true,
        forceStaleCache: false,
    },
    expandedPermitId: null,
    formStep: 3,

    setActiveNav: (nav) => set({ activeNav: nav }),

    setFailureSimulator: (key, value) =>
        set((state) => ({
            failures: {
                ...state.failures,
                [key]: value,
            },
        })),

    setExpandedPermitId: (id) => set({ expandedPermitId: id }),

    setFormStep: (step) => set({ formStep: step }),
}));
