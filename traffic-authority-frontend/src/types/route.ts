export interface RouteDecompositionRequest {
    origin: string;
    destination: string;
    mapVersion?: string;
}

export interface RouteSegmentPreview {
    segmentId: string;
    authoritativeRegion: string;
    timeWindowStart: string;
    timeWindowEnd: string;
}

export interface RouteDecompositionResponse {
    mapVersion: string;
    segments: RouteSegmentPreview[];
}

export interface RouteSegment {
    segmentId: string;
    originNode: string;
    destinationNode: string;
    authoritativeRegion: string;
    description?: string;
    active: boolean;
}

export interface MapVersionInfo {
    version: string;
    description?: string;
    isCurrent: boolean;
    createdAt?: string;
}
