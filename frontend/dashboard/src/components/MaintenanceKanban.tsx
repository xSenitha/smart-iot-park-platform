import React from 'react';
import type { MaintenanceTicket, TicketStatus } from '../types';
import { Wrench, Plus, ArrowRight, UserCheck, CheckCircle2 } from 'lucide-react';

interface MaintenanceKanbanProps {
  tickets: MaintenanceTicket[];
  onUpdateStatus: (id: number, status: TicketStatus) => void;
  onOpenNewTicketModal: () => void;
}

export const MaintenanceKanban: React.FC<MaintenanceKanbanProps> = ({
  tickets,
  onUpdateStatus,
  onOpenNewTicketModal
}) => {
  const columns: { status: TicketStatus; label: string; color: string }[] = [
    { status: 'NEW', label: 'Triage / New', color: 'var(--amber-400)' },
    { status: 'ASSIGNED', label: 'Assigned to Crew', color: 'var(--cyan-400)' },
    { status: 'IN_PROGRESS', label: 'In Progress / On-Site', color: 'var(--violet-400)' },
    { status: 'RESOLVED', label: 'Completed & Resolved', color: 'var(--emerald-400)' }
  ];

  const getPriorityBadge = (priority: MaintenanceTicket['priority']) => {
    switch (priority) {
      case 'URGENT':
        return <span className="badge badge-critical">URGENT</span>;
      case 'HIGH':
        return <span className="badge badge-warning">HIGH</span>;
      case 'MEDIUM':
        return <span className="badge badge-online">MEDIUM</span>;
      case 'LOW':
        return <span className="badge" style={{ background: 'rgba(255, 255, 255, 0.08)', color: 'var(--text-muted)' }}>LOW</span>;
    }
  };

  const getNextStatus = (current: TicketStatus): TicketStatus | null => {
    switch (current) {
      case 'NEW':
        return 'ASSIGNED';
      case 'ASSIGNED':
        return 'IN_PROGRESS';
      case 'IN_PROGRESS':
        return 'RESOLVED';
      default:
        return null;
    }
  };

  return (
    <div className="glass-card" style={{ padding: '24px', margin: '0 24px 24px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Wrench size={20} color="var(--violet-400)" />
          <div>
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Maintenance Dispatch & Work Orders Kanban</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Municipal field operations workflow and technician assignment
            </p>
          </div>
        </div>

        <button onClick={onOpenNewTicketModal} className="btn btn-primary" id="btn-create-work-order">
          <Plus size={16} /> New Work Order
        </button>
      </div>

      {/* Kanban Board Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
        gap: '16px',
        alignItems: 'start'
      }}>
        {columns.map(col => {
          const colTickets = tickets.filter(t => t.status === col.status);

          return (
            <div
              key={col.status}
              style={{
                background: 'rgba(7, 11, 20, 0.7)',
                borderRadius: '10px',
                border: '1px solid var(--border-subtle)',
                padding: '14px',
                minHeight: '400px',
                display: 'flex',
                flexDirection: 'column',
                gap: '12px'
              }}
            >
              {/* Column Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '10px', borderBottom: '1px solid var(--border-subtle)' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: col.color }}>
                  {col.label}
                </span>
                <span style={{
                  background: 'rgba(255, 255, 255, 0.08)',
                  padding: '2px 8px',
                  borderRadius: '12px',
                  fontSize: '0.75rem',
                  fontFamily: 'var(--font-mono)',
                  color: 'var(--text-secondary)'
                }}>
                  {colTickets.length}
                </span>
              </div>

              {/* Tickets Column */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', flex: 1 }}>
                {colTickets.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '30px 10px', color: 'var(--text-muted)', fontSize: '0.78rem' }}>
                    No tickets in this stage
                  </div>
                ) : (
                  colTickets.map(ticket => {
                    const nextStatus = getNextStatus(ticket.status);

                    return (
                      <div
                        key={ticket.id}
                        style={{
                          background: 'rgba(15, 23, 42, 0.85)',
                          borderRadius: '8px',
                          border: '1px solid rgba(255, 255, 255, 0.08)',
                          padding: '12px',
                          display: 'flex',
                          flexDirection: 'column',
                          gap: '8px',
                          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)'
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <span style={{ fontSize: '0.72rem', fontFamily: 'var(--font-mono)', color: 'var(--cyan-400)' }}>
                            {ticket.ticketNumber}
                          </span>
                          {getPriorityBadge(ticket.priority)}
                        </div>

                        <h4 style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                          {ticket.title}
                        </h4>

                        <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                          {ticket.description}
                        </p>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '6px', paddingTop: '8px', borderTop: '1px solid rgba(255, 255, 255, 0.05)', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <UserCheck size={12} color="var(--emerald-400)" />
                            {ticket.assignedTo || 'Unassigned'}
                          </span>
                          {ticket.deviceUid && (
                            <span style={{ fontFamily: 'var(--font-mono)' }}>{ticket.deviceUid}</span>
                          )}
                        </div>

                        {/* Advance Action Button */}
                        {nextStatus && (
                          <button
                            onClick={() => onUpdateStatus(ticket.id, nextStatus)}
                            className="btn btn-secondary"
                            style={{ width: '100%', justifyContent: 'center', padding: '6px', fontSize: '0.75rem', marginTop: '4px' }}
                          >
                            <span>Move to {nextStatus.replace('_', ' ')}</span>
                            <ArrowRight size={13} />
                          </button>
                        )}

                        {ticket.status === 'RESOLVED' && (
                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px', color: 'var(--emerald-400)', fontSize: '0.72rem', fontWeight: 600 }}>
                            <CheckCircle2 size={13} /> Completed
                          </div>
                        )}
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
