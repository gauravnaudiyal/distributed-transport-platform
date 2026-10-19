import { useEffect } from 'react';
import { Topbar } from './components/layout/Topbar';
import { Ticker } from './components/layout/Ticker';
import { Sidebar } from './components/layout/Sidebar';
import { Center } from './components/center/Center';
import { RightPanel } from './components/layout/RightPanel';
import { Footer } from './components/layout/Footer';
import { LoginModal } from './components/auth/LoginModal';
import { usePermitStore } from './store/permits';
import { useSystemStore } from './store/system';
import { useMockData } from './hooks/useMockData';
import { useAuth } from './context/AuthContext';
import './App.css';

function App() {
  const { permits, auditTrail, currentRequest } = usePermitStore();
  const { healthCheckpoints, regionalCapacity, congestedCorridors, globalCongestion } =
    useSystemStore();
  const { isLoggedIn, isAdmin, isCivilian } = useAuth();

  // Load mock data on mount
  useMockData();

  if (!isLoggedIn) {
    return <LoginModal />;
  }

  // Civilian layout: simplified 2-column (Sidebar + Center only)
  if (isCivilian) {
    return (
      <div className="bg-traffic-bg text-traffic-text min-h-screen w-screen cursor-crosshair overflow-x-hidden">
        {/* Scanlines effect */}
        <div
          className="fixed inset-0 pointer-events-none z-50"
          style={{
            background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0, 255, 136, 0.03) 2px, rgba(0, 255, 136, 0.03) 4px)',
            animation: 'scanMove 8s linear infinite',
          }}
        />

        {/* Topbar */}
        <Topbar />

        {/* Ticker */}
        <Ticker />

        {/* Civilian Layout - 2 columns (Sidebar + Center) */}
        <div
          className="grid grid-cols-[260px_1fr] gap-0"
          style={{ minHeight: 'calc(100vh - 86px)' }}
        >
          {/* Sidebar */}
          <div className="overflow-hidden">
            <Sidebar healthCheckpoints={healthCheckpoints} />
          </div>

          {/* Center Content */}
          <div className="overflow-hidden">
            <Center
              permits={permits}
              auditTrail={auditTrail}
              currentRequest={currentRequest}
            />
          </div>
        </div>

        {/* Footer */}
        <Footer />
      </div>
    );
  }

  // Admin layout: full 3-column + footer
  return (
    <div className="bg-traffic-bg text-traffic-text min-h-screen w-screen cursor-crosshair overflow-x-hidden">
      {/* Scanlines effect */}
      <div
        className="fixed inset-0 pointer-events-none z-50"
        style={{
          background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0, 255, 136, 0.03) 2px, rgba(0, 255, 136, 0.03) 4px)',
          animation: 'scanMove 8s linear infinite',
        }}
      />

      {/* Topbar */}
      <Topbar />

      {/* Ticker */}
      <Ticker />

      {/* Admin Layout - 3 columns × 2 rows */}
      <div
        className="grid grid-cols-[260px_1fr_280px] grid-rows-[1fr_auto] gap-0"
        style={{ minHeight: 'calc(100vh - 86px)' }}
      >
        {/* Sidebar - row 1, col 1 */}
        <div className="overflow-hidden row-start-1 col-start-1">
          <Sidebar healthCheckpoints={healthCheckpoints} />
        </div>

        {/* Center Content - row 1, col 2 */}
        <div className="overflow-hidden row-start-1 col-start-2">
          <Center
            permits={permits}
            auditTrail={auditTrail}
            currentRequest={currentRequest}
          />
        </div>

        {/* Right Panel - row 1, col 3 */}
        <div className="overflow-hidden row-start-1 col-start-3">
          <RightPanel
            regionalCapacity={regionalCapacity}
            congestedCorridors={congestedCorridors}
            globalCongestion={globalCongestion}
          />
        </div>

        {/* Footer - row 2, spans all columns */}
        <div className="row-start-2 col-start-1 col-end-4">
          <Footer />
        </div>
      </div>
    </div>
  );
}

export default App;
