import React from 'react';
import type { Device } from '../types';
import { X, BatteryCharging, Wrench, Calendar, MapPin, Cpu } from 'lucide-react';

interface DeviceDetailModalProps {
  device: Device | null;
  onClose: () => void;
  onCreateTicket: (device: Device) => void;
}

export const DeviceDetailModal: React.FC<DeviceDetailModalProps> = ({
  device,
  onClose,
  onCreateTicket
}) => {
  if (!device) return null;

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      background: 'rgba(0, 0, 0, 0.75)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 100,
      padding: '20px'
    }}>
      <div className="glass-card" style={{
        background: '#0d1424',
        border: '1px solid rgba(255, 255, 255, 0.15)',
        width: '100%',
        maxWidth: '520px',
        padding: '24px',
        borderRadius: '16px',
        boxShadow: '0 20px 50px rgba(0, 0, 0, 0.6)'
      }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className={`badge ${device.status === 'ONLINE' ? 'badge-online' : 'badge-warning'}`}>
                {device.status}
              </span>
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem', color: 'var(--cyan-400)' }}>
                {device.deviceUid}
              </span>
            </div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '4px' }}>
              {device.name}
            </h3>
          </div>

          <button
            onClick={onClose}
            style={{
              background: 'rgba(255, 255, 255, 0.05)',
              border: 'none',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              padding: '6px',
              borderRadius: '8px'
            }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Device Metrics Breakdown */}
        <div style={{
          background: 'rgba(7, 11, 20, 0.7)',
          borderRadius: '10px',
          padding: '16px',
          border: '1px solid var(--border-subtle)',
          display: 'grid',
          gridTemplateColumns: 'repeat(2, 1fr)',
          gap: '12px',
          marginBottom: '20px'
        }}>
          <div>
            <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Battery Level</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
              <BatteryCharging size={16} color="var(--emerald-400)" />
              <span style={{ fontSize: '1.1rem', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
                {device.batteryLevel}%
              </span>
            </div>
          </div>

          <div>
            <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Asset Type</span>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '2px' }}>
              {device.type}
            </div>
          </div>

          {device.type === 'BIN' && (
            <>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Fill Level</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: (device.metrics?.fillPercentage || 0) >= 80 ? 'var(--rose-400)' : 'var(--emerald-400)', marginTop: '2px' }}>
                  {device.metrics?.fillPercentage}%
                </div>
              </div>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Tilt Angle</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '2px' }}>
                  {device.metrics?.tiltAngle || 1.2}°
                </div>
              </div>
            </>
          )}

          {device.type === 'SOIL' && (
            <>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Soil Moisture</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--cyan-400)', marginTop: '2px' }}>
                  {device.metrics?.soilMoisture}%
                </div>
              </div>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Soil Temp</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--amber-400)', marginTop: '2px' }}>
                  {device.metrics?.soilTemperature || 21.5}°C
                </div>
              </div>
            </>
          )}

          {device.type === 'LIGHT' && (
            <>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Ambient Lux</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--amber-400)', marginTop: '2px' }}>
                  {device.metrics?.lightLux?.toLocaleString()} lx
                </div>
              </div>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Power Draw</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '2px' }}>
                  {device.metrics?.powerConsumptionWatts} W
                </div>
              </div>
            </>
          )}
        </div>

        {/* Metadata info */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <MapPin size={14} color="var(--emerald-400)" />
            <span>Zone: <strong>Zone {device.zoneId}</strong> &middot; Coordinates: ({device.x}, {device.y})</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Cpu size={14} color="var(--cyan-400)" />
            <span>Firmware: <strong>{device.firmwareVersion}</strong> &middot; MQTT QoS: <strong>1</strong></span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Calendar size={14} color="var(--violet-400)" />
            <span>Last Heartbeat: <strong>{new Date(device.lastHeartbeat).toLocaleTimeString()}</strong></span>
          </div>
        </div>

        {/* Footer Actions */}
        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
          <button onClick={onClose} className="btn btn-secondary">
            Close
          </button>
          <button
            onClick={() => {
              onCreateTicket(device);
              onClose();
            }}
            className="btn btn-primary"
          >
            <Wrench size={15} /> Create Work Order
          </button>
        </div>
      </div>
    </div>
  );
};
