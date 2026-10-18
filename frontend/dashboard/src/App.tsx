import { useState, useEffect } from 'react';
import type { Role, Device, Zone, Alert, MaintenanceTicket, AIPrediction, ParkKPIs } from './types';
import { apiClient } from './services/api';
import { Header } from './components/Header';
import { KpiCards } from './components/KpiCards';
import { ParkMap } from './components/ParkMap';
import { TelemetryCharts } from './components/TelemetryCharts';
import { AlertCenter } from './components/AlertCenter';
import { MaintenanceKanban } from './components/MaintenanceKanban';
import { AIPredictions } from './components/AIPredictions';
import { DeviceDetailModal } from './components/DeviceDetailModal';
import { NewTicketModal } from './components/NewTicketModal';
import { Map, Activity, AlertTriangle, Wrench, Brain } from 'lucide-react';
import './App.css';

export function App() {
  const [role, setRole] = useState<Role>('ROLE_CITY_PLANNER');
  const [activeTab, setActiveTab] = useState<'MAP' | 'ANALYTICS' | 'ALERTS' | 'KANBAN' | 'AI'>('MAP');
  
  const [zones, setZones] = useState<Zone[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [tickets, setTickets] = useState<MaintenanceTicket[]>([]);
  const [predictions, setPredictions] = useState<AIPrediction[]>([]);
  const [kpis, setKpis] = useState<ParkKPIs>({
    parkHealthScore: 92,
    totalDevices: 15,
    onlineDevices: 15,
    activeAlertsCount: 2,
    criticalAlertsCount: 1,
    openTicketsCount: 2,
    averageBattery: 93
  });

  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
  const [selectedZoneId, setSelectedZoneId] = useState<number | undefined>(undefined);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isTicketModalOpen, setIsTicketModalOpen] = useState(false);
  const [ticketModalPrefill, setTicketModalPrefill] = useState<{
    deviceUid?: string;
    zoneId?: number;
    title?: string;
    description?: string;
  }>({});

  const loadData = async () => {
    setIsRefreshing(true);
    try {
      const [z, d, a, t, p, k] = await Promise.all([
        apiClient.getZones(),
        apiClient.getDevices(),
        apiClient.getAlerts(),
        apiClient.getTickets(),
        apiClient.getPredictions(),
        apiClient.getKpis()
      ]);
      setZones(z);
      setDevices(d);
      setAlerts(a);
      setTickets(t);
      setPredictions(p);
      setKpis(k);
    } catch (err) {
      console.error('Failed to load park telemetry:', err);
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
    // Background polling/refresh ticker every 15s
    const timer = setInterval(() => {
      loadData();
    }, 15000);
    return () => clearInterval(timer);
  }, []);

  const handleAcknowledgeAlert = async (id: number) => {
    await apiClient.acknowledgeAlert(id);
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, status: 'ACKNOWLEDGED' } : a));
  };

  const handleResolveAlert = async (id: number) => {
    await apiClient.resolveAlert(id);
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, status: 'RESOLVED', resolvedAt: new Date().toISOString() } : a));
    setKpis(prev => ({ ...prev, activeAlertsCount: Math.max(0, prev.activeAlertsCount - 1) }));
  };

  const handleCreateTicketFromAlert = (alert: Alert) => {
    setTicketModalPrefill({
      deviceUid: alert.deviceUid,
      zoneId: alert.zoneId,
      title: `Work Order: ${alert.title}`,
      description: `Investigate and resolve alert: ${alert.message}`
    });
    setIsTicketModalOpen(true);
  };

  const handleCreateTicketFromDevice = (device: Device) => {
    setTicketModalPrefill({
      deviceUid: device.deviceUid,
      zoneId: device.zoneId,
      title: `Asset Inspection: ${device.name}`,
      description: `Perform physical inspection and diagnostic on ${device.deviceUid} (${device.type}).`
    });
    setIsTicketModalOpen(true);
  };

  const handleUpdateTicketStatus = async (id: number, status: MaintenanceTicket['status']) => {
    await apiClient.updateTicketStatus(id, status);
    setTickets(prev => prev.map(t => t.id === id ? { ...t, status } : t));
    if (status === 'RESOLVED') {
      setKpis(prev => ({ ...prev, openTicketsCount: Math.max(0, prev.openTicketsCount - 1) }));
    }
  };

  const handleCreateTicket = async (ticket: Partial<MaintenanceTicket>) => {
    const created = await apiClient.createTicket(ticket);
    setTickets(prev => [created, ...prev]);
    setKpis(prev => ({ ...prev, openTicketsCount: prev.openTicketsCount + 1 }));
  };

  const handleTriggerBatchJob = async () => {
    await apiClient.triggerAiBatchJob();
    const updatedPredictions = await apiClient.getPredictions();
    setPredictions(updatedPredictions);
  };

  return (
    <div className="dashboard-app" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Header */}
      <Header
        currentRole={role}
        onRoleChange={setRole}
        onRefresh={loadData}
        isRefreshing={isRefreshing}
      />

      {/* Main KPI Ribbon */}
      <KpiCards
        kpis={kpis}
        onOpenAlerts={() => setActiveTab('ALERTS')}
        onOpenKanban={() => setActiveTab('KANBAN')}
      />

      {/* Navigation Sub-Header */}
      <nav style={{
        display: 'flex',
        gap: '8px',
        padding: '0 24px',
        marginBottom: '20px',
        overflowX: 'auto'
      }}>
        <button
          onClick={() => setActiveTab('MAP')}
          className={`nav-tab ${activeTab === 'MAP' ? 'active' : ''}`}
          id="tab-map"
        >
          <Map size={16} /> Park Spatial Map
        </button>
        <button
          onClick={() => setActiveTab('ANALYTICS')}
          className={`nav-tab ${activeTab === 'ANALYTICS' ? 'active' : ''}`}
          id="tab-analytics"
        >
          <Activity size={16} /> Telemetry & Curves
        </button>
        <button
          onClick={() => setActiveTab('ALERTS')}
          className={`nav-tab ${activeTab === 'ALERTS' ? 'active' : ''}`}
          id="tab-alerts"
        >
          <AlertTriangle size={16} /> Alert Center ({alerts.filter(a => a.status !== 'RESOLVED').length})
        </button>
        <button
          onClick={() => setActiveTab('KANBAN')}
          className={`nav-tab ${activeTab === 'KANBAN' ? 'active' : ''}`}
          id="tab-kanban"
        >
          <Wrench size={16} /> Maintenance Kanban
        </button>
        <button
          onClick={() => setActiveTab('AI')}
          className={`nav-tab ${activeTab === 'AI' ? 'active' : ''}`}
          id="tab-ai"
        >
          <Brain size={16} /> AI Predictive Insights
        </button>
      </nav>

      {/* Dynamic Tab Body */}
      <main style={{ flex: 1 }}>
        {activeTab === 'MAP' && (
          <ParkMap
            devices={devices}
            zones={zones}
            onSelectDevice={setSelectedDevice}
            selectedZoneId={selectedZoneId}
            onSelectZone={setSelectedZoneId}
          />
        )}

        {activeTab === 'ANALYTICS' && (
          <TelemetryCharts devices={devices} />
        )}

        {activeTab === 'ALERTS' && (
          <AlertCenter
            alerts={alerts}
            onAcknowledge={handleAcknowledgeAlert}
            onResolve={handleResolveAlert}
            onCreateTicketFromAlert={handleCreateTicketFromAlert}
          />
        )}

        {activeTab === 'KANBAN' && (
          <MaintenanceKanban
            tickets={tickets}
            onUpdateStatus={handleUpdateTicketStatus}
            onOpenNewTicketModal={() => {
              setTicketModalPrefill({});
              setIsTicketModalOpen(true);
            }}
          />
        )}

        {activeTab === 'AI' && (
          <AIPredictions
            predictions={predictions}
            onTriggerBatchJob={handleTriggerBatchJob}
          />
        )}
      </main>

      {/* Footer */}
      <footer style={{
        padding: '16px 24px',
        textAlign: 'center',
        borderTop: '1px solid var(--border-subtle)',
        color: 'var(--text-muted)',
        fontSize: '0.8rem',
        marginTop: 'auto'
      }}>
        Smart IoT Park Platform &middot; Microservices Architecture &middot; Java Spring Boot &middot; MongoDB &middot; FastAPI ML &middot; React 18
      </footer>

      {/* Device Inspector Modal */}
      <DeviceDetailModal
        device={selectedDevice}
        onClose={() => setSelectedDevice(null)}
        onCreateTicket={handleCreateTicketFromDevice}
      />

      {/* New Maintenance Ticket Modal */}
      <NewTicketModal
        isOpen={isTicketModalOpen}
        onClose={() => setIsTicketModalOpen(false)}
        onSubmit={handleCreateTicket}
        prefilledDeviceUid={ticketModalPrefill.deviceUid}
        prefilledZoneId={ticketModalPrefill.zoneId}
        prefilledTitle={ticketModalPrefill.title}
        prefilledDescription={ticketModalPrefill.description}
      />
    </div>
  );
}

export default App;
