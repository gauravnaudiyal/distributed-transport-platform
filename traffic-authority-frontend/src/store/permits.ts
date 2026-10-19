import { create } from 'zustand';
import type { PermitItem, AuditTrailEvent, UserPermitRequest } from '../types/index';

interface PermitState {
    permits: PermitItem[];
    auditTrail: AuditTrailEvent[];
    currentRequest: UserPermitRequest;
    selectedPermitId: string | null;
}

interface PermitActions {
    setPermits: (permits: PermitItem[]) => void;
    setAuditTrail: (events: AuditTrailEvent[]) => void;
    setCurrentRequest: (request: Partial<UserPermitRequest>) => void;
    setSelectedPermitId: (id: string | null) => void;
    addPermit: (permit: PermitItem) => void;
}

type PermitStateWithActions = PermitState & PermitActions;

export const usePermitStore = create<PermitStateWithActions>((set) => ({
    permits: [],
    auditTrail: [],
    currentRequest: {
        originSector: 'London, UK — Zone EU-WEST-4',
        destinationSector: 'Amsterdam, NL — Zone EU-WEST-7',
        vehicleType: 'CIVILIAN SEDAN',
        occupancy: '2 of 5',
    },
    selectedPermitId: null,

    setPermits: (permits) => set({ permits }),
    setAuditTrail: (auditTrail) => set({ auditTrail }),
    setCurrentRequest: (request) =>
        set((state) => ({
            currentRequest: { ...state.currentRequest, ...request },
        })),
    setSelectedPermitId: (id) => set({ selectedPermitId: id }),
    addPermit: (permit) =>
        set((state) => ({
            permits: [...state.permits, permit],
        })),
}));
