import { create } from 'zustand';
import type { CompatibilityResponse } from '../types/compatibility';
import type { RouteDecompositionResponse } from '../types/route';

export type BookingState = 'PENDING' | 'RESERVED' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'FAILED';

export interface RouteSegment {
    id: string;
    name: string;
    ownerRegion: string;
    capacity: number;
    available: number;
    closure?: boolean;
    restriction?: string;
}

export interface Booking {
    id: string;
    origin: string;
    destination: string;
    departureTime: string;
    arrivalTime: string;
    segments: RouteSegment[];
    state: BookingState;
    createdAt: string;
    verificationToken?: string;
    mapVersion: string;
}

export interface BookingRequest {
    origin: string;
    destination: string;
    departureTime: string;
    timeWindow: number; // minutes
}

interface BookingStore {
    bookings: Booking[];
    currentBooking: Booking | null;
    isLoading: boolean;
    error: string | null;
    compatibilityResult: CompatibilityResponse | null;
    routePreview: RouteDecompositionResponse | null;

    // Actions
    setCurrentBooking: (booking: Booking | null) => void;
    addBooking: (booking: Booking) => void;
    updateBookingState: (bookingId: string, state: BookingState) => void;
    setError: (error: string | null) => void;
    setLoading: (loading: boolean) => void;
    setCompatibilityResult: (result: CompatibilityResponse | null) => void;
    setRoutePreview: (preview: RouteDecompositionResponse | null) => void;
    clearError: () => void;
    cancelBooking: (bookingId: string) => void;
}

export const useBookingStore = create<BookingStore>((set) => ({
    bookings: [],
    currentBooking: null,
    isLoading: false,
    error: null,
    compatibilityResult: null,
    routePreview: null,

    setCurrentBooking: (booking) => set({ currentBooking: booking }),

    addBooking: (booking) =>
        set((state) => ({
            bookings: [...state.bookings, booking],
            currentBooking: booking,
        })),

    updateBookingState: (bookingId, state) =>
        set((current) => ({
            bookings: current.bookings.map((b) =>
                b.id === bookingId ? { ...b, state } : b
            ),
            currentBooking:
                current.currentBooking?.id === bookingId
                    ? { ...current.currentBooking, state }
                    : current.currentBooking,
        })),

    setError: (error) => set({ error }),

    setLoading: (loading) => set({ isLoading: loading }),

    setCompatibilityResult: (result) => set({ compatibilityResult: result }),

    setRoutePreview: (preview) => set({ routePreview: preview }),

    clearError: () => set({ error: null }),

    cancelBooking: (bookingId) =>
        set((state) => ({
            bookings: state.bookings.map((b) =>
                b.id === bookingId ? { ...b, state: 'CANCELLED' } : b
            ),
            currentBooking:
                state.currentBooking?.id === bookingId
                    ? { ...state.currentBooking, state: 'CANCELLED' }
                    : state.currentBooking,
        })),
}));
