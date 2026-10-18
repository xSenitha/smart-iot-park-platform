import React from 'react';
import type { ParkKPIs } from '../types';
import { ShieldCheck, Wifi, AlertTriangle, Wrench, BatteryCharging } from 'lucide-react';

interface KpiCardsProps {
  kpis: ParkKPIs;
  onOpenAlerts: () => void;
  onOpenKanban: () => void;
}

export const KpiCards: React.FC<KpiCardsProps> = ({ kpis, onOpenAlerts, onOpenKanban }) => {

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
      gap: '16px',
      margin: '0 24px 24px 24px'
    }}>
      {/* 1. Park Health Score */}
      <div className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', position: 'relative', overflow: 'hidden' }}>
        <div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
            Park Health Index
          </span>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginTop: '6px' }}>
            <span style={{ fontSize: '2.1rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--emerald-400)' }}>
              {kpis.parkHealthScore}%
            </span>
            <span style={{ fontSize: '0.8rem', color: 'var(--emerald-400)', fontWeight: 600 }}>Optimal</span>
          </div>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Weighted across 3 zones & 15 nodes
          </p>
        </div>

        {/* Circular SVG Ring */}
        <div style={{ position: 'relative', width: '56px', height: '56px' }}>
          <svg viewBox="0 0 36 36" style={{ transform: 'rotate(-90deg)', width: '100%', height: '100%' }}>
            <path
              d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              fill="none"
              stroke="rgba(255, 255, 255, 0.08)"
              strokeWidth="3.5"
            />
            <path
              d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              fill="none"
              stroke="var(--emerald-400)"
              strokeDasharray={`${kpis.parkHealthScore}, 100`}
              strokeWidth="3.5"
              strokeLinecap="round"
            />
          </svg>
          <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <ShieldCheck size={20} color="var(--emerald-400)" />
          </div>
        </div>
      </div>

      {/* 2. Connected IoT Fleet */}
      <div className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
            IoT Sensor Fleet
          </span>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginTop: '6px' }}>
            <span style={{ fontSize: '2.1rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--cyan-400)' }}>
              {kpis.onlineDevices} <span style={{ fontSize: '1.2rem', color: 'var(--text-muted)' }}>/ {kpis.totalDevices}</span>
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
            <BatteryCharging size={14} color="var(--emerald-400)" />
            <span>Avg Battery: <strong>{kpis.averageBattery}%</strong></span>
          </div>
        </div>

        <div style={{
          width: '50px',
          height: '50px',
          borderRadius: '12px',
          background: 'rgba(6, 182, 212, 0.1)',
          border: '1px solid rgba(6, 182, 212, 0.25)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <Wifi size={24} color="var(--cyan-400)" />
        </div>
      </div>

      {/* 3. Open Incidents & Alerts */}
      <div
        className="glass-card"
        onClick={onOpenAlerts}
        style={{ padding: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' }}
      >
        <div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
            Incident Alerts
          </span>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginTop: '6px' }}>
            <span style={{ fontSize: '2.1rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: kpis.criticalAlertsCount > 0 ? 'var(--rose-400)' : 'var(--amber-400)' }}>
              {kpis.activeAlertsCount}
            </span>
            {kpis.criticalAlertsCount > 0 && (
              <span className="badge badge-critical" style={{ fontSize: '0.7rem' }}>
                {kpis.criticalAlertsCount} Critical
              </span>
            )}
          </div>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Click to open Alert Center feed
          </p>
        </div>

        <div style={{
          width: '50px',
          height: '50px',
          borderRadius: '12px',
          background: kpis.criticalAlertsCount > 0 ? 'rgba(244, 63, 94, 0.1)' : 'rgba(245, 158, 11, 0.1)',
          border: kpis.criticalAlertsCount > 0 ? '1px solid rgba(244, 63, 94, 0.3)' : '1px solid rgba(245, 158, 11, 0.3)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <AlertTriangle size={24} color={kpis.criticalAlertsCount > 0 ? 'var(--rose-400)' : 'var(--amber-400)'} />
        </div>
      </div>

      {/* 4. Maintenance Work Orders */}
      <div
        className="glass-card"
        onClick={onOpenKanban}
        style={{ padding: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' }}
      >
        <div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
            Work Orders Desk
          </span>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginTop: '6px' }}>
            <span style={{ fontSize: '2.1rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--violet-400)' }}>
              {kpis.openTicketsCount}
            </span>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Open Work Orders</span>
          </div>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
            View Maintenance Kanban Desk
          </p>
        </div>

        <div style={{
          width: '50px',
          height: '50px',
          borderRadius: '12px',
          background: 'rgba(139, 92, 246, 0.1)',
          border: '1px solid rgba(139, 92, 246, 0.3)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <Wrench size={24} color="var(--violet-400)" />
        </div>
      </div>
    </div>
  );
};
