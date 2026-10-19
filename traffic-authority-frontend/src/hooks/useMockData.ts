import { useEffect } from 'react';
import { usePermitStore } from '../store/permits';
import { useSystemStore } from '../store/system';
import type { } from '../types/index';
import {
    mockPermits,
    mockHealthCheckpoints,
    mockRegionalCapacity,
    mockCongestedCorridors,
    mockAuditTrail,
} from '../utils/mockData';

export function useMockData() {
    const { setPermits, setAuditTrail } = usePermitStore();
    const {
        setHealthCheckpoints,
        setRegionalCapacity,
        setCongestedCorridors,
    } = useSystemStore();

    useEffect(() => {
        // Simulate API calls
        setPermits(mockPermits);
        setAuditTrail(mockAuditTrail);
        setHealthCheckpoints(mockHealthCheckpoints);
        setRegionalCapacity(mockRegionalCapacity);
        setCongestedCorridors(mockCongestedCorridors);
    }, []);
}
