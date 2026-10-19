import { Broadcast } from './Broadcast';
import { ActivePermits } from './ActivePermits';
import { AuditTrail } from './AuditTrail';
import { BookingJourneyForm } from './BookingJourneyForm';
import { BookingStatus } from './BookingStatus';
import { RoutePreviewPanel } from './RoutePreviewPanel';
import { TrafficMapPanel } from './TrafficMapPanel';
import { AuthorityDashboard } from '../authority/AuthorityDashboard';
import { UserProfilePanel } from '../profile/UserProfilePanel';
import { UserPreferencesPanel } from '../profile/UserPreferencesPanel';
import { NotificationTimeline } from '../profile/NotificationTimeline';
import type { PermitItem, AuditTrailEvent, UserPermitRequest } from '../../types/index';
import { useAuth } from '../../context/AuthContext';
import { useBookingStore } from '../../store/booking';
import { useUIStore } from '../../store/ui';

interface CenterProps {
    permits: PermitItem[];
    auditTrail: AuditTrailEvent[];
    currentRequest: UserPermitRequest;
    onPermitClick?: (id: string) => void;
}

export function Center({
    permits,
    auditTrail,
    currentRequest,
    onPermitClick,
}: CenterProps) {
    const { isCivilian, isAdmin } = useAuth();
    const { currentBooking } = useBookingStore();
    const { activeNav } = useUIStore();

    return (
        <div className="bg-traffic-bg-2 overflow-y-auto h-full">
            {isCivilian ? (
                // CIVILIAN VIEW - Role-based tab navigation
                <div className="px-8 py-7">
                    <Broadcast />

                    {activeNav === 'my-permits' ? (
                        // MY PERMITS TAB: Show permits + audit trail
                        <>
                            <div className="flex items-start justify-between mb-7">
                                <div>
                                    <div className="font-barlow font-black text-2xl uppercase tracking-wider text-traffic-white">
                                        My Permits
                                    </div>
                                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mt-1.5">
                                        // View your active permits and transaction history
                                    </div>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-6">
                                <ActivePermits permits={permits} onPermitClick={onPermitClick} />
                                <AuditTrail events={auditTrail} />
                            </div>
                        </>
                    ) : activeNav === 'profile-notifications' ? (
                        <>
                            <div className="flex items-start justify-between mb-7">
                                <div>
                                    <div className="font-barlow font-black text-2xl uppercase tracking-wider text-traffic-white">
                                        Profile & Notifications
                                    </div>
                                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mt-1.5">
                                        // Test user profile, preferences, and notification APIs
                                    </div>
                                </div>
                            </div>
                            <div className="grid grid-cols-2 gap-6">
                                <div className="space-y-6">
                                    <UserProfilePanel />
                                    <UserPreferencesPanel />
                                </div>
                                <NotificationTimeline />
                            </div>
                        </>
                    ) : activeNav === 'traffic-map' ? (
                        <>
                            <div className="flex items-start justify-between mb-7">
                                <div>
                                    <div className="font-barlow font-black text-2xl uppercase tracking-wider text-traffic-white">
                                        Traffic Map
                                    </div>
                                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mt-1.5">
                                        // Live segment and map version view from route service
                                    </div>
                                </div>
                            </div>
                            <TrafficMapPanel />
                        </>
                    ) : (
                        // REQUEST PASSAGE TAB: Show booking form
                        <>
                            <div className="flex items-start justify-between mb-7">
                                <div>
                                    <div className="font-barlow font-black text-2xl uppercase tracking-wider text-traffic-white">
                                        Request Passage
                                    </div>
                                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mt-1.5">
                                        // Book a journey across regional networks
                                    </div>
                                </div>
                            </div>

                            <div className="space-y-6">
                                <BookingJourneyForm />
                                <RoutePreviewPanel />
                                {currentBooking && (
                                    <BookingStatus booking={currentBooking} />
                                )}
                            </div>
                        </>
                    )}
                </div>
            ) : (
                // ADMIN VIEW - Authority Dashboard
                <AuthorityDashboard />
            )}
        </div>
    );
}
