import type { Device, Zone, Alert, MaintenanceTicket, AIPrediction, ParkKPIs } from '../types';

const CORE_API_URL = '/api/v1';
export const INGESTION_API_URL = '/api/v1/telemetry';
const AI_ENGINE_URL = '/api/v1';

// Seeded state matching database baseline for fallback & demo
const MOCK_ZONES: Zone[] = [
  { id: 1, parkId: 1, name: 'Central Plaza & Promenade', code: 'ZONE-01-PLAZA', type: 'PLAZA', healthScore: 94.5, activeSensors: 6, targetMoisture: 45.0, areaSqMeters: 18500 },
  { id: 2, parkId: 1, name: 'Botanical Gardens & Arboretum', code: 'ZONE-02-GARDEN', type: 'GARDEN', healthScore: 88.0, activeSensors: 5, targetMoisture: 55.0, areaSqMeters: 32000 },
  { id: 3, parkId: 1, name: 'Athletics & Sports Complex', code: 'ZONE-03-SPORTS', type: 'SPORTS', healthScore: 91.2, activeSensors: 4, targetMoisture: 40.0, areaSqMeters: 24000 }
];

const MOCK_DEVICES: Device[] = [
  // Zone 1
  { id: 1, deviceUid: 'DEV-BIN-0001', name: 'Plaza North Compact Bin', type: 'BIN', status: 'ONLINE', parkId: 1, zoneId: 1, batteryLevel: 92.5, firmwareVersion: 'v1.4.2', lastHeartbeat: new Date().toISOString(), x: 260, y: 180, metrics: { fillPercentage: 58.4, tiltAngle: 1.2, batteryLevel: 92.5 } },
  { id: 2, deviceUid: 'DEV-BIN-0002', name: 'Plaza Fountain Bin', type: 'BIN', status: 'ONLINE', parkId: 1, zoneId: 1, batteryLevel: 89.0, firmwareVersion: 'v1.4.2', lastHeartbeat: new Date().toISOString(), x: 310, y: 240, metrics: { fillPercentage: 84.1, tiltAngle: 2.1, batteryLevel: 89.0 } },
  { id: 7, deviceUid: 'DEV-SOIL-0001', name: 'Central Lawn Agronomic Probe', type: 'SOIL', status: 'ONLINE', parkId: 1, zoneId: 1, batteryLevel: 96.0, firmwareVersion: 'v2.0.1', lastHeartbeat: new Date().toISOString(), x: 350, y: 190, metrics: { soilMoisture: 42.8, soilTemperature: 21.4, batteryLevel: 96.0 } },
  { id: 8, deviceUid: 'DEV-SOIL-0002', name: 'Plaza Hedge Moisture Sensor', type: 'SOIL', status: 'ONLINE', parkId: 1, zoneId: 1, batteryLevel: 94.2, firmwareVersion: 'v2.0.1', lastHeartbeat: new Date().toISOString(), x: 220, y: 220, metrics: { soilMoisture: 39.5, soilTemperature: 21.8, batteryLevel: 94.2 } },
  { id: 12, deviceUid: 'DEV-LIGHT-0001', name: 'Promenade Luminaire Pole 1', type: 'LIGHT', status: 'ONLINE', parkId: 1, zoneId: 1, batteryLevel: 99.1, firmwareVersion: 'v1.1.0', lastHeartbeat: new Date().toISOString(), x: 280, y: 130, metrics: { isLampOn: false, lightLux: 45000, powerConsumptionWatts: 0.0, batteryLevel: 99.1 } },
  { id: 13, deviceUid: 'DEV-LIGHT-0002', name: 'Promenade Luminaire Pole 2', type: 'LIGHT', status: 'ONLINE', parkId: 1, zoneId: 1, batteryLevel: 98.4, firmwareVersion: 'v1.1.0', lastHeartbeat: new Date().toISOString(), x: 340, y: 140, metrics: { isLampOn: false, lightLux: 46200, powerConsumptionWatts: 0.0, batteryLevel: 98.4 } },

  // Zone 2
  { id: 3, deviceUid: 'DEV-BIN-0003', name: 'Rose Garden Pergola Bin', type: 'BIN', status: 'ONLINE', parkId: 1, zoneId: 2, batteryLevel: 91.0, firmwareVersion: 'v1.4.2', lastHeartbeat: new Date().toISOString(), x: 550, y: 260, metrics: { fillPercentage: 42.0, tiltAngle: 0.8, batteryLevel: 91.0 } },
  { id: 4, deviceUid: 'DEV-BIN-0004', name: 'Greenhouse Visitors Bin', type: 'BIN', status: 'WARNING', parkId: 1, zoneId: 2, batteryLevel: 82.3, firmwareVersion: 'v1.4.2', lastHeartbeat: new Date().toISOString(), x: 620, y: 310, metrics: { fillPercentage: 88.5, tiltAngle: 1.5, batteryLevel: 82.3 } },
  { id: 9, deviceUid: 'DEV-SOIL-0003', name: 'Botanical Orchid Deep Probe', type: 'SOIL', status: 'ONLINE', parkId: 1, zoneId: 2, batteryLevel: 95.0, firmwareVersion: 'v2.0.1', lastHeartbeat: new Date().toISOString(), x: 580, y: 210, metrics: { soilMoisture: 48.2, soilTemperature: 22.0, batteryLevel: 95.0 } },
  { id: 10, deviceUid: 'DEV-SOIL-0004', name: 'Arboretum Canopy Moisture', type: 'SOIL', status: 'ONLINE', parkId: 1, zoneId: 2, batteryLevel: 93.8, firmwareVersion: 'v2.0.1', lastHeartbeat: new Date().toISOString(), x: 670, y: 240, metrics: { soilMoisture: 51.5, soilTemperature: 21.1, batteryLevel: 93.8 } },
  { id: 14, deviceUid: 'DEV-LIGHT-0003', name: 'Botanical Pathway LED Mast', type: 'LIGHT', status: 'ONLINE', parkId: 1, zoneId: 2, batteryLevel: 99.5, firmwareVersion: 'v1.1.0', lastHeartbeat: new Date().toISOString(), x: 520, y: 340, metrics: { isLampOn: false, lightLux: 42000, powerConsumptionWatts: 0.0, batteryLevel: 99.5 } },

  // Zone 3
  { id: 5, deviceUid: 'DEV-BIN-0005', name: 'Athletics Bleachers Bin', type: 'BIN', status: 'ONLINE', parkId: 1, zoneId: 3, batteryLevel: 88.0, firmwareVersion: 'v1.4.2', lastHeartbeat: new Date().toISOString(), x: 230, y: 440, metrics: { fillPercentage: 62.0, tiltAngle: 1.0, batteryLevel: 88.0 } },
  { id: 6, deviceUid: 'DEV-BIN-0006', name: 'Tennis Pavilion Bin', type: 'BIN', status: 'ONLINE', parkId: 1, zoneId: 3, batteryLevel: 87.4, firmwareVersion: 'v1.4.2', lastHeartbeat: new Date().toISOString(), x: 310, y: 490, metrics: { fillPercentage: 35.0, tiltAngle: 0.5, batteryLevel: 87.4 } },
  { id: 11, deviceUid: 'DEV-SOIL-0005', name: 'Main Pitch Turf Moisture', type: 'SOIL', status: 'ONLINE', parkId: 1, zoneId: 3, batteryLevel: 92.1, firmwareVersion: 'v2.0.1', lastHeartbeat: new Date().toISOString(), x: 270, y: 400, metrics: { soilMoisture: 38.0, soilTemperature: 23.5, batteryLevel: 92.1 } },
  { id: 15, deviceUid: 'DEV-LIGHT-0004', name: 'Sports Field Floodlight Alpha', type: 'LIGHT', status: 'ONLINE', parkId: 1, zoneId: 3, batteryLevel: 99.8, firmwareVersion: 'v1.1.0', lastHeartbeat: new Date().toISOString(), x: 370, y: 460, metrics: { isLampOn: false, lightLux: 48000, powerConsumptionWatts: 0.0, batteryLevel: 99.8 } }
];

let mockAlerts: Alert[] = [
  { id: 1, deviceId: 4, deviceUid: 'DEV-BIN-0004', zoneId: 2, alertType: 'BIN_OVERFLOW_WARNING', severity: 'WARNING', title: 'High Fill Level: Greenhouse Bin', message: 'Sensor reporting fill capacity at 88.5%. Approaching 90% threshold.', status: 'OPEN', createdAt: new Date(Date.now() - 3600000).toISOString() },
  { id: 2, deviceId: 2, deviceUid: 'DEV-BIN-0002', zoneId: 1, alertType: 'AI_PREDICTED_OVERFLOW', severity: 'CRITICAL', title: 'AI Forecast: Plaza Fountain Bin overflow in 1.8h', message: 'Fill velocity spiked to 8.2%/h. Overflow predicted before 14:00.', status: 'OPEN', createdAt: new Date(Date.now() - 1800000).toISOString() },
  { id: 3, deviceId: 11, deviceUid: 'DEV-SOIL-0005', zoneId: 3, alertType: 'SOIL_MOISTURE_LOW', severity: 'WARNING', title: 'Irrigation Needed: Main Pitch Turf', message: 'Soil moisture dipped to 38.0% (target 40.0%). Deficit threshold approaching.', status: 'ACKNOWLEDGED', createdAt: new Date(Date.now() - 7200000).toISOString() }
];

let mockTickets: MaintenanceTicket[] = [
  { id: 1, ticketNumber: 'WO-20261018-001', parkId: 1, zoneId: 2, deviceId: 4, deviceUid: 'DEV-BIN-0004', title: 'Empty Greenhouse Visitors Bin', description: 'Fill percentage exceeded 88%. Dispatch collection crew for perimeter sweep.', priority: 'HIGH', status: 'ASSIGNED', assignedTo: 'Dave Miller (Crew Alpha)', createdAt: new Date(Date.now() - 3600000).toISOString() },
  { id: 2, ticketNumber: 'WO-20261018-002', parkId: 1, zoneId: 1, deviceId: 2, deviceUid: 'DEV-BIN-0002', title: 'Urgent Bin Emptying: Plaza Fountain', description: 'AI predictive overflow alert triggered. Required within 2 hours.', priority: 'URGENT', status: 'NEW', assignedTo: 'Unassigned', createdAt: new Date(Date.now() - 1800000).toISOString() },
  { id: 3, ticketNumber: 'WO-20261017-004', parkId: 1, zoneId: 3, deviceId: 15, deviceUid: 'DEV-LIGHT-0004', title: 'Calibrate Floodlight Photocell', description: 'Quarterly sensor recalibration and lux check.', priority: 'LOW', status: 'RESOLVED', assignedTo: 'Sarah Jenkins', createdAt: new Date(Date.now() - 86400000).toISOString(), resolvedAt: new Date(Date.now() - 36000000).toISOString() }
];

let mockPredictions: AIPrediction[] = [
  {
    id: 'pred-1',
    predictionType: 'BIN_OVERFLOW',
    deviceId: 'DEV-BIN-0002',
    zoneId: 1,
    currentFill: 84.1,
    hoursUntilFull: 1.8,
    fillRatePerHour: 8.2,
    riskLevel: 'CRITICAL',
    confidence: 0.94,
    recommendation: 'Immediate waste collection required within 2 hours',
    generatedAt: new Date().toISOString()
  },
  {
    id: 'pred-2',
    predictionType: 'BIN_OVERFLOW',
    deviceId: 'DEV-BIN-0004',
    zoneId: 2,
    currentFill: 88.5,
    hoursUntilFull: 2.4,
    fillRatePerHour: 4.8,
    riskLevel: 'HIGH',
    confidence: 0.91,
    recommendation: 'Schedule waste collection in upcoming dispatch shift',
    generatedAt: new Date().toISOString()
  },
  {
    id: 'pred-3',
    predictionType: 'EQUIPMENT_ANOMALY',
    deviceId: 'DEV-LIGHT-0001',
    zoneId: 1,
    anomalyScore: 0.12,
    isAnomaly: false,
    probableRootCause: 'NORMAL',
    recommendation: 'Device operating within acceptable bounds.',
    confidence: 0.89,
    generatedAt: new Date().toISOString()
  },
  {
    id: 'pred-4',
    predictionType: 'IRRIGATION',
    zoneId: 3,
    riskLevel: 'MEDIUM',
    recommendation: 'Run 15 min supplemental twilight irrigation cycle (21:30 UTC).',
    confidence: 0.95,
    generatedAt: new Date().toISOString()
  }
];

export const apiClient = {
  // --- Zones & KPIs ---
  async getZones(): Promise<Zone[]> {
    try {
      const res = await fetch(`${CORE_API_URL}/zones`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) return data;
      }
    } catch {
      // fallback
    }
    return MOCK_ZONES;
  },

  async getKpis(): Promise<ParkKPIs> {
    const zones = await this.getZones();
    const devices = await this.getDevices();
    const alerts = await this.getAlerts();
    const tickets = await this.getTickets();

    const avgHealth = Math.round(zones.reduce((acc, z) => acc + (z.healthScore || 90), 0) / (zones.length || 1));
    const openAlerts = alerts.filter(a => a.status !== 'RESOLVED');
    const criticalAlerts = openAlerts.filter(a => a.severity === 'CRITICAL');
    const openTickets = tickets.filter(t => t.status !== 'RESOLVED' && t.status !== 'CANCELLED');
    const avgBattery = Math.round(devices.reduce((acc, d) => acc + (d.batteryLevel || 90), 0) / (devices.length || 1));

    return {
      parkHealthScore: avgHealth,
      totalDevices: devices.length,
      onlineDevices: devices.filter(d => d.status === 'ONLINE').length,
      activeAlertsCount: openAlerts.length,
      criticalAlertsCount: criticalAlerts.length,
      openTicketsCount: openTickets.length,
      averageBattery: avgBattery
    };
  },

  // --- Devices ---
  async getDevices(): Promise<Device[]> {
    try {
      const res = await fetch(`${CORE_API_URL}/devices`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) return data;
      }
    } catch {
      // fallback
    }
    return MOCK_DEVICES;
  },

  // --- Alerts ---
  async getAlerts(): Promise<Alert[]> {
    try {
      const res = await fetch(`${CORE_API_URL}/alerts`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) return data;
      }
    } catch {
      // fallback
    }
    return mockAlerts;
  },

  async acknowledgeAlert(id: number): Promise<Alert> {
    try {
      const res = await fetch(`${CORE_API_URL}/alerts/${id}/acknowledge`, { method: 'PUT' });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }
    const alert = mockAlerts.find(a => a.id === id);
    if (alert) {
      alert.status = 'ACKNOWLEDGED';
    }
    return alert || mockAlerts[0];
  },

  async resolveAlert(id: number): Promise<Alert> {
    try {
      const res = await fetch(`${CORE_API_URL}/alerts/${id}/resolve`, { method: 'PUT' });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }
    const alert = mockAlerts.find(a => a.id === id);
    if (alert) {
      alert.status = 'RESOLVED';
      alert.resolvedAt = new Date().toISOString();
    }
    return alert || mockAlerts[0];
  },

  // --- Maintenance Tickets ---
  async getTickets(): Promise<MaintenanceTicket[]> {
    try {
      const res = await fetch(`${CORE_API_URL}/maintenance/tickets`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) return data;
      }
    } catch {
      // fallback
    }
    return mockTickets;
  },

  async createTicket(ticket: Partial<MaintenanceTicket>): Promise<MaintenanceTicket> {
    try {
      const res = await fetch(`${CORE_API_URL}/maintenance/tickets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(ticket)
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }
    const newTicket: MaintenanceTicket = {
      id: mockTickets.length + 1,
      ticketNumber: `WO-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-00${mockTickets.length + 1}`,
      parkId: ticket.parkId || 1,
      zoneId: ticket.zoneId || 1,
      deviceId: ticket.deviceId,
      deviceUid: ticket.deviceUid,
      title: ticket.title || 'General Maintenance Inspection',
      description: ticket.description || '',
      priority: ticket.priority || 'MEDIUM',
      status: 'NEW',
      assignedTo: ticket.assignedTo || 'Unassigned',
      createdAt: new Date().toISOString()
    };
    mockTickets = [newTicket, ...mockTickets];
    return newTicket;
  },

  async updateTicketStatus(id: number, status: MaintenanceTicket['status']): Promise<MaintenanceTicket> {
    try {
      const res = await fetch(`${CORE_API_URL}/maintenance/tickets/${id}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }
    const t = mockTickets.find(item => item.id === id);
    if (t) {
      t.status = status;
      t.updatedAt = new Date().toISOString();
      if (status === 'RESOLVED') {
        t.resolvedAt = new Date().toISOString();
      }
    }
    return t || mockTickets[0];
  },

  // --- AI Predictions ---
  async getPredictions(): Promise<AIPrediction[]> {
    try {
      const res = await fetch(`${AI_ENGINE_URL}/predictions/latest`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) return data;
      }
    } catch {
      // fallback
    }
    return mockPredictions;
  },

  async triggerAiBatchJob(): Promise<{ status: string; devicesEvaluated: number; alertsTriggered: number }> {
    try {
      const res = await fetch(`${AI_ENGINE_URL}/jobs/predict-batch`, { method: 'POST' });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }
    // Refresh predictions in mock store
    mockPredictions = [
      {
        id: `pred-${Date.now()}`,
        predictionType: 'BIN_OVERFLOW',
        deviceId: 'DEV-BIN-0002',
        zoneId: 1,
        currentFill: 86.3,
        hoursUntilFull: 1.4,
        fillRatePerHour: 8.5,
        riskLevel: 'CRITICAL',
        confidence: 0.96,
        recommendation: 'Immediate dispatch required: overflow imminent at 13:45 UTC',
        generatedAt: new Date().toISOString()
      },
      ...mockPredictions
    ];
    return {
      status: 'COMPLETED',
      devicesEvaluated: 15,
      alertsTriggered: 1
    };
  }
};
