import { create } from 'zustand';

export interface RoadClosure {
    id: string;
    segmentId: string;
    segmentName: string;
    reason: string;
    startTime: string;
    endTime: string;
    severity: 'critical' | 'high' | 'medium' | 'low';
    createdBy: string;
    createdAt: string;
}

export interface CapacityOverride {
    id: string;
    segmentId: string;
    segmentName: string;
    originalCapacity: number;
    overrideCapacity: number;
    reason: string;
    effectiveFrom: string;
    effectiveUntil: string;
    createdBy: string;
    createdAt: string;
}

export interface SegmentRestriction {
    id: string;
    segmentId: string;
    segmentName: string;
    restriction: string;
    affectedTime: string;
    createdBy: string;
    createdAt: string;
}

interface AuthorityStore {
    closures: RoadClosure[];
    overrides: CapacityOverride[];
    restrictions: SegmentRestriction[];
    isLoading: boolean;
    isPolicyUpdateInFlight: boolean;
    lastPolicyApplyAt: string | null;
    error: string | null;

    // Closures
    addClosure: (closure: RoadClosure) => void;
    removeClosure: (closureId: string) => void;

    // Capacity Overrides
    addOverride: (override: CapacityOverride) => void;
    removeOverride: (overrideId: string) => void;

    // Restrictions
    addRestriction: (restriction: SegmentRestriction) => void;
    removeRestriction: (restrictionId: string) => void;

    // State management
    setLoading: (loading: boolean) => void;
    setPolicyUpdateInFlight: (inFlight: boolean) => void;
    setLastPolicyApplyAt: (timestamp: string | null) => void;
    setError: (error: string | null) => void;
    clearError: () => void;
}

export const useAuthorityStore = create<AuthorityStore>((set) => ({
    closures: [],
    overrides: [],
    restrictions: [],
    isLoading: false,
    isPolicyUpdateInFlight: false,
    lastPolicyApplyAt: null,
    error: null,

    addClosure: (closure) =>
        set((state) => ({
            closures: [...state.closures, closure],
        })),

    removeClosure: (closureId) =>
        set((state) => ({
            closures: state.closures.filter((c) => c.id !== closureId),
        })),

    addOverride: (override) =>
        set((state) => ({
            overrides: [...state.overrides, override],
        })),

    removeOverride: (overrideId) =>
        set((state) => ({
            overrides: state.overrides.filter((o) => o.id !== overrideId),
        })),

    addRestriction: (restriction) =>
        set((state) => ({
            restrictions: [...state.restrictions, restriction],
        })),

    removeRestriction: (restrictionId) =>
        set((state) => ({
            restrictions: state.restrictions.filter((r) => r.id !== restrictionId),
        })),

    setLoading: (loading) => set({ isLoading: loading }),

    setPolicyUpdateInFlight: (inFlight) => set({ isPolicyUpdateInFlight: inFlight }),

    setLastPolicyApplyAt: (timestamp) => set({ lastPolicyApplyAt: timestamp }),

    setError: (error) => set({ error }),

    clearError: () => set({ error: null }),
}));
