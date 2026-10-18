import React from 'react';
import type { Role } from '../types';
import { Shield, RefreshCw, Clock } from 'lucide-react';

interface HeaderProps {
  currentRole: Role;
  onRoleChange: (role: Role) => void;
  onRefresh: () => void;
  isRefreshing: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  currentRole,
  onRoleChange,
  onRefresh,
  isRefreshing
}) => {
  const getRoleLabel = (role: Role) => {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'System Administrator';
      case 'ROLE_CITY_PLANNER':
        return 'City Planner (Urban Ops)';
      case 'ROLE_MAINTENANCE_CREW':
        return 'Maintenance Dispatch Crew';
    }
  };

  return (
    <header className="glass-card" style={{ padding: '16px 24px', margin: '16px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{
          width: '44px',
          height: '44px',
          borderRadius: '12px',
          background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(6, 182, 212, 0.2))',
          border: '1px solid rgba(16, 185, 129, 0.4)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 0 16px rgba(16, 185, 129, 0.3)'
        }}>
          <span style={{ fontSize: '24px' }}>🌿</span>
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h1 style={{ fontSize: '1.35rem', fontWeight: 700, letterSpacing: '-0.02em', background: 'linear-gradient(135deg, #fff, #94a3b8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Smart IoT Park Platform
            </h1>
            <span className="badge badge-online">
              <span className="pulse-dot online"></span>
              Live Grid Active
            </span>
          </div>
          <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginTop: '2px' }}>
            Grand Central Memorial Park &middot; 15 IoT Sensors &middot; Microservices Mesh
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255, 255, 255, 0.04)', padding: '6px 12px', borderRadius: '8px', border: '1px solid var(--border-subtle)', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          <Clock size={15} color="var(--emerald-400)" />
          <span style={{ fontFamily: 'var(--font-mono)' }}>UTC 14:00 &middot; Synced</span>
        </div>

        {/* Role Selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255, 255, 255, 0.04)', padding: '6px 12px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
          <Shield size={16} color="var(--cyan-400)" />
          <label htmlFor="role-select" title={getRoleLabel(currentRole)} style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Role:</label>
          <select
            id="role-select"
            value={currentRole}
            onChange={(e) => onRoleChange(e.target.value as Role)}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--text-primary)',
              fontFamily: 'var(--font-sans)',
              fontSize: '0.85rem',
              fontWeight: 600,
              cursor: 'pointer',
              outline: 'none'
            }}
          >
            <option value="ROLE_CITY_PLANNER" style={{ background: '#0f172a' }}>City Planner</option>
            <option value="ROLE_ADMIN" style={{ background: '#0f172a' }}>Administrator</option>
            <option value="ROLE_MAINTENANCE_CREW" style={{ background: '#0f172a' }}>Maintenance Crew</option>
          </select>
        </div>

        {/* Sync / Refresh Button */}
        <button
          id="btn-sync-telemetry"
          onClick={onRefresh}
          className="btn btn-secondary"
          title="Force telemetry sync"
          style={{ padding: '8px 12px' }}
        >
          <RefreshCw size={15} className={isRefreshing ? 'spin-anim' : ''} />
          <span>Sync</span>
        </button>
      </div>
    </header>
  );
};
