import { httpClient } from './httpClient';
import type {
    MapVersionInfo,
    RouteDecompositionRequest,
    RouteDecompositionResponse,
    RouteSegment,
} from '../types/route';

const API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

const ROUTE_BASE =
    import.meta.env.VITE_ROUTE_API_BASE_URL ||
    API_BASE ||
    import.meta.env.VITE_GATEWAY_BASE_URL ||
    '/api';

interface BackendRouteDecomposeResponse {
    map_version: string;
    segments: Array<{
        segment_id: string;
        authoritative_region: string;
        time_window_start: string;
        time_window_end: string;
    }>;
}

export const routeApiService = {
    async decomposeRoute(
        payload: RouteDecompositionRequest
    ): Promise<RouteDecompositionResponse> {
        const response = await httpClient.post<BackendRouteDecomposeResponse>(
            '/routes/decompose',
            {
                origin: payload.origin,
                destination: payload.destination,
                map_version: payload.mapVersion,
            },
            { baseUrl: ROUTE_BASE }
        );

        return {
            mapVersion: response.map_version,
            segments: response.segments.map((segment) => ({
                segmentId: segment.segment_id,
                authoritativeRegion: segment.authoritative_region,
                timeWindowStart: segment.time_window_start,
                timeWindowEnd: segment.time_window_end,
            })),
        };
    },

    async getSegments(): Promise<RouteSegment[]> {
        const response = await httpClient.get<RouteSegment[] | null>('/routes/segments', {
            baseUrl: ROUTE_BASE,
        });
        if (!Array.isArray(response)) {
            return [];
        }

        return response.map((segment) => ({
            segmentId: segment.segmentId,
            originNode: segment.originNode,
            destinationNode: segment.destinationNode,
            authoritativeRegion: segment.authoritativeRegion,
            description: segment.description,
            active: segment.active,
        }));
    },

    async getCurrentMapVersion(): Promise<MapVersionInfo> {
        return httpClient.get<MapVersionInfo>('/routes/map-version/current', {
            baseUrl: ROUTE_BASE,
        });
    },
};
