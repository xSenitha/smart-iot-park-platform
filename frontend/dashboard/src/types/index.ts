export type Role = 'ROLE_ADMIN' | 'ROLE_CITY_PLANNER' | 'ROLE_MAINTENANCE_CREW';

export interface User {
  id: number;
  username: string;
  email: string;
  role: Role;
  token?: string;
}

export type DeviceType = 'BIN' | 'SOIL' | 'LIGHT';
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'WARNING' | 'MAINTENANCE' | 'CRITICAL';

export interface Device {
  id: number;
  deviceUid: string;
  name: string;
  type: DeviceType;
  status: DeviceStatus;
  parkId: number;
  zoneId: number;
  batteryLevel: number;
  firmwareVersion: string;
  lastHeartbeat: string;
  x: number;
  y: number;
  metrics?: {
    fillPercentage?: number;
    tiltAngle?: number;
    soilMoisture?: number;
    soilTemperature?: number;
    lightLux?: number;
    isLampOn?: boolean;
    powerConsumptionWatts?: number;
    batteryLevel?: number;
  };
}

export interface Zone {
  id: number;
  parkId: number;
  name: string;
  code: string;
  type: string;
  healthScore: number;
  activeSensors: number;
  targetMoisture: number;
  areaSqMeters: number;
}

export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL';
export type AlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface Alert {
  id: number;
  deviceId?: number;
  deviceUid?: string;
  zoneId?: number;
  alertType: string;
  severity: AlertSeverity;
  title: string;
  message: string;
  status: AlertStatus;
  createdAt: string;
  resolvedAt?: string;
}

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type TicketStatus = 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CANCELLED';

export interface MaintenanceTicket {
  id: number;
  ticketNumber: string;
  parkId: number;
  zoneId: number;
  deviceId?: number;
  deviceUid?: string;
  title: string;
  description: string;
  priority: TicketPriority;
  status: TicketStatus;
  assignedTo?: string;
  createdAt: string;
  updatedAt?: string;
  resolvedAt?: string;
}

export interface AIPrediction {
  id?: string;
  predictionType: 'BIN_OVERFLOW' | 'EQUIPMENT_ANOMALY' | 'IRRIGATION';
  deviceId?: string;
  zoneId?: number;
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  currentFill?: number;
  hoursUntilFull?: number;
  fillRatePerHour?: number;
  anomalyScore?: number;
  isAnomaly?: boolean;
  probableRootCause?: string;
  recommendation: string;
  confidence?: number;
  generatedAt: string;
}

export interface ParkKPIs {
  parkHealthScore: number;
  totalDevices: number;
  onlineDevices: number;
  activeAlertsCount: number;
  criticalAlertsCount: number;
  openTicketsCount: number;
  averageBattery: number;
}
