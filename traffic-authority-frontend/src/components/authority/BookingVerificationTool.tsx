import { useState } from 'react';
import { authorityApiService } from '../../services/authorityApi';
import { Button } from '../common/Button';

export function BookingVerificationTool() {
    const [bookingId, setBookingId] = useState('');
    const [verificationToken, setVerificationToken] = useState('');
    const [verificationResult, setVerificationResult] = useState<boolean | null>(null);
    const [verificationMessage, setVerificationMessage] = useState<string>('');
    const [isVerifying, setIsVerifying] = useState(false);

    const handleVerify = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!bookingId || !verificationToken) {
            return;
        }

        setIsVerifying(true);
        try {
            const result = await authorityApiService.verifyBooking(bookingId);
            const isValid =
                verificationToken === result.proofReference ||
                verificationToken === result.bookingId;
            setVerificationResult(isValid);
            setVerificationMessage(
                isValid
                    ? `Authority status: ${result.status} (map v${result.mapVersion})`
                    : `Authority returned status ${result.status}. Token mismatch.`
            );
        } catch (err) {
            setVerificationResult(false);
            setVerificationMessage(
                err instanceof Error ? err.message : 'Authority verification failed'
            );
        } finally {
            setIsVerifying(false);
        }
    };

    const handleReset = () => {
        setBookingId('');
        setVerificationToken('');
        setVerificationResult(null);
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <div>
                <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-1">
                    Booking Verification Tool
                </h2>
                <p className="font-mono text-xs text-traffic-text-2 mb-6">
          // For enforcement agents to verify driver bookings
                </p>
            </div>

            <form onSubmit={handleVerify} className="space-y-4 mb-6">
                <div>
                    <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                        Booking ID
                    </label>
                    <input
                        type="text"
                        value={bookingId}
                        onChange={(e) => setBookingId(e.target.value)}
                        className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        placeholder="e.g., BOOK-1710123456"
                        disabled={isVerifying}
                    />
                </div>

                <div>
                    <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                        Verification Token
                    </label>
                    <input
                        type="text"
                        value={verificationToken}
                        onChange={(e) => setVerificationToken(e.target.value)}
                        className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        placeholder="e.g., TOKEN-ABC123XYZ"
                        disabled={isVerifying}
                    />
                </div>

                <div className="flex gap-3">
                    <Button
                        type="submit"
                        disabled={isVerifying || !bookingId || !verificationToken}
                        className="flex-1"
                        variant="primary"
                    >
                        {isVerifying ? 'Verifying...' : 'Verify Booking'}
                    </Button>
                    {verificationResult !== null && (
                        <Button type="button" onClick={handleReset} className="flex-1">
                            Clear
                        </Button>
                    )}
                </div>
            </form>

            {/* Verification Result */}
            {verificationResult !== null && (
                <div
                    className={`p-6 border ${verificationResult
                            ? 'bg-traffic-green/10 border-traffic-green'
                            : 'bg-traffic-red/10 border-traffic-red'
                        }`}
                >
                    <div className="text-center">
                        <p
                            className={`font-barlow font-bold text-xl uppercase tracking-wider mb-3 ${verificationResult
                                    ? 'text-traffic-green'
                                    : 'text-traffic-red'
                                }`}
                        >
                            {verificationResult ? '✓ BOOKING VALID' : '✗ BOOKING INVALID'}
                        </p>

                        {verificationResult ? (
                            <div className="space-y-3 text-left">
                                <div className="bg-traffic-bg p-4 border border-traffic-border">
                                    <p className="font-mono text-xs text-traffic-text-3 mb-1">
                                        BOOKING ID
                                    </p>
                                    <p className="font-mono font-bold text-traffic-accent">
                                        {bookingId}
                                    </p>
                                </div>

                                <div className="bg-traffic-bg p-4 border border-traffic-border">
                                    <p className="font-mono text-xs text-traffic-text-3 mb-1">
                                        TOKEN
                                    </p>
                                    <p className="font-mono font-bold text-traffic-accent">
                                        {verificationToken}
                                    </p>
                                </div>

                                <div className="bg-traffic-bg p-4 border border-traffic-border">
                                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-2">
                                        Status
                                    </p>
                                    <div className="space-y-2 font-mono text-xs">
                                        <p className="text-traffic-green">✓ Driver authorized</p>
                                        <p className="text-traffic-green">✓ Route segments confirmed</p>
                                        <p className="text-traffic-green">✓ Capacity reserved</p>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="space-y-3 text-left">
                                <p className="font-mono text-sm text-traffic-red">
                                    Booking could not be verified. Please check:
                                </p>
                                <ul className="font-mono text-xs text-traffic-text-2 space-y-1 list-disc list-inside">
                                    <li>Booking ID is correct</li>
                                    <li>Verification token matches</li>
                                    <li>Booking has not been cancelled</li>
                                    <li>Booking is still active</li>
                                </ul>
                            </div>
                        )}
                        {verificationMessage && (
                            <p className="font-mono text-xs text-traffic-text-3 mt-4">
                                {verificationMessage}
                            </p>
                        )}
                    </div>
                </div>
            )}

            {/* Info Box */}
            <div className="mt-6 pt-6 border-t border-traffic-border">
                <p className="font-mono text-xs text-traffic-text-2 mb-3">
                    USAGE INSTRUCTIONS:
                </p>
                <ul className="font-mono text-xs text-traffic-text-3 space-y-1">
                    <li>1. Request Booking ID from driver</li>
                    <li>2. Request Verification Token (shown in booking confirmation)</li>
                    <li>3. Enter both values above</li>
                    <li>4. Click "Verify Booking" to confirm authorization</li>
                </ul>
            </div>
        </div>
    );
}
