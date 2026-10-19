export type CompatibilityStatus = 'APPROVED' | 'REJECTED' | 'ERROR';

export interface CompatibilityRequest {
    bookingId: string;
    routeId: string;
    routeName: string;
    travelDate: string;
    travelTime: string;
    vehicleType: 'CAR' | 'BUS' | 'TRUCK';
    passengerCount: number;
    driverId: string;
}

export interface CompatibilityResponse {
    bookingId: string;
    status: CompatibilityStatus;
    message: string;
    currentLoad: number;
    maxCapacity: number;
    fallbackUsed: boolean;
}
