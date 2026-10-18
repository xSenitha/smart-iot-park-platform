import React, { useState } from 'react';
import type { Alert, AlertSeverity } from '../types';
import { AlertTriangle, CheckCircle, Clock, Check, PlusCircle, Radio } from 'lucide-react';

interface AlertCenterProps {
  alerts: Alert[];
  onAcknowledge: (id: number) => void;
  onResolve: (id: number) => void;
  onCreateTicketFromAlert: (alert: Alert) => void;
}

export const AlertCenter: React.FC<AlertCenterProps> = ({
  alerts,
  onAcknowledge,
  onResolve,
  onCreateTicketFromAlert
}) => {
  const [severityFilter, setSeverityFilter] = useState<'ALL' | AlertSeverity>('ALL');

  const filteredAlerts = alerts.filter(a => {
    if (severityFilter !== 'ALL' && a.severity !== severityFilter) return false;
    return true;
  });

  return (
    <div className="glass-card" style={{ padding: '24px', margin: '0 24px 24px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <AlertTriangle size={20} color="var(--rose-400)" />
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Incident Alert Center</h2>
          <span className="badge badge-online" style={{ fontSize: '0.7rem' }}>
            <Radio size={12} className="pulse-anim" /> Live SSE Stream
          </span>
        </div>

        {/* Severity Filter Tabs */}
        <div style={{ display: 'flex', background: 'rgba(255, 255, 255, 0.04)', borderRadius: '8px', padding: '3px', border: '1px solid var(--border-subtle)' }}>
          {(['ALL', 'CRITICAL', 'WARNING', 'INFO'] as const).map(sev => (
            <button
              key={sev}
              onClick={() => setSeverityFilter(sev)}
              style={{
                background: severityFilter === sev ? 'rgba(244, 63, 94, 0.2)' : 'transparent',
                color: severityFilter === sev ? 'var(--rose-400)' : 'var(--text-secondary)',
                border: 'none',
                padding: '4px 12px',
                borderRadius: '6px',
                fontSize: '0.78rem',
                cursor: 'pointer',
                fontWeight: 600
              }}
            >
              {sev}
            </button>
          ))}
        </div>
      </div>

      {/* Alert Feed List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {filteredAlerts.length === 0 ? (
          <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-muted)' }}>
            <CheckCircle size={32} color="var(--emerald-400)" style={{ margin: '0 auto 8px auto' }} />
            <p>No active incidents matching the selected criteria.</p>
          </div>
        ) : (
          filteredAlerts.map(alert => {
            const isCritical = alert.severity === 'CRITICAL';
            const isWarning = alert.severity === 'WARNING';
            const badgeClass = isCritical ? 'badge-critical' : isWarning ? 'badge-warning' : 'badge-online';

            return (
              <div
                key={alert.id}
                style={{
                  background: 'rgba(15, 23, 42, 0.65)',
                  border: isCritical ? '1px solid rgba(244, 63, 94, 0.35)' : '1px solid var(--border-subtle)',
                  borderRadius: '10px',
                  padding: '16px 20px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '16px',
                  transition: 'background 0.2s ease'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '14px', flex: 1, minWidth: '280px' }}>
                  <div style={{
                    marginTop: '2px',
                    width: '36px',
                    height: '36px',
                    borderRadius: '8px',
                    background: isCritical ? 'rgba(244, 63, 94, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0
                  }}>
                    <AlertTriangle size={18} color={isCritical ? 'var(--rose-400)' : 'var(--amber-400)'} />
                  </div>

                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                      <span className={`badge ${badgeClass}`}>{alert.severity}</span>
                      {alert.deviceUid && (
                        <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--cyan-400)' }}>
                          {alert.deviceUid}
                        </span>
                      )}
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Clock size={12} /> {new Date(alert.createdAt).toLocaleTimeString()}
                      </span>
                    </div>

                    <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-primary)', marginTop: '4px' }}>
                      {alert.title}
                    </h4>
                    <p style={{ fontSize: '0.825rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                      {alert.message}
                    </p>
                  </div>
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {alert.status === 'OPEN' && (
                    <button
                      onClick={() => onAcknowledge(alert.id)}
                      className="btn btn-secondary"
                      style={{ padding: '6px 12px', fontSize: '0.78rem' }}
                    >
                      <Check size={14} /> Acknowledge
                    </button>
                  )}
                  {alert.status !== 'RESOLVED' && (
                    <>
                      <button
                        onClick={() => onCreateTicketFromAlert(alert)}
                        className="btn btn-primary"
                        style={{ padding: '6px 12px', fontSize: '0.78rem' }}
                      >
                        <PlusCircle size={14} /> Dispatch Ticket
                      </button>
                      <button
                        onClick={() => onResolve(alert.id)}
                        className="btn btn-secondary"
                        style={{ padding: '6px 10px', fontSize: '0.78rem' }}
                      >
                        Resolve
                      </button>
                    </>
                  )}
                  {alert.status === 'RESOLVED' && (
                    <span className="badge badge-online">RESOLVED</span>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
