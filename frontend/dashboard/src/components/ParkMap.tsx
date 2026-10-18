import React, { useState } from 'react';
import type { Device, Zone } from '../types';
import { Trash2, Sprout, Lightbulb, Compass } from 'lucide-react';

interface ParkMapProps {
  devices: Device[];
  zones: Zone[];
  onSelectDevice: (device: Device) => void;
  selectedZoneId?: number;
  onSelectZone: (zoneId?: number) => void;
}

export const ParkMap: React.FC<ParkMapProps> = ({
  devices,
  zones,
  onSelectDevice,
  selectedZoneId,
  onSelectZone
}) => {
  const [deviceFilter, setDeviceFilter] = useState<'ALL' | 'BIN' | 'SOIL' | 'LIGHT'>('ALL');
  const [hoveredDevice, setHoveredDevice] = useState<Device | null>(null);

  const filteredDevices = devices.filter(d => {
    if (deviceFilter !== 'ALL' && d.type !== deviceFilter) return false;
    if (selectedZoneId && d.zoneId !== selectedZoneId) return false;
    return true;
  });

  const getPinColor = (device: Device) => {
    if (device.type === 'BIN' && (device.metrics?.fillPercentage || 0) >= 85) return 'var(--rose-400)';
    if (device.type === 'BIN' && (device.metrics?.fillPercentage || 0) >= 70) return 'var(--amber-400)';
    if (device.type === 'SOIL' && (device.metrics?.soilMoisture || 0) <= 20) return 'var(--rose-400)';
    if (device.type === 'SOIL' && (device.metrics?.soilMoisture || 0) <= 38) return 'var(--amber-400)';
    if (device.status === 'WARNING') return 'var(--amber-400)';
    if (device.status === 'CRITICAL') return 'var(--rose-400)';
    return 'var(--emerald-400)';
  };

  const getDeviceIcon = (type: Device['type']) => {
    switch (type) {
      case 'BIN':
        return <Trash2 size={13} />;
      case 'SOIL':
        return <Sprout size={13} />;
      case 'LIGHT':
        return <Lightbulb size={13} />;
    }
  };

  return (
    <div className="glass-card" style={{ padding: '24px', margin: '0 24px 24px 24px' }}>
      {/* Map Control Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Compass size={20} color="var(--emerald-400)" />
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Interactive Park & Zone Spatial Grid</h2>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Showing {filteredDevices.length} of {devices.length} IoT Assets
          </span>
        </div>

        {/* Filter Toolbar */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          {/* Zone Selector */}
          <div style={{ display: 'flex', background: 'rgba(255, 255, 255, 0.04)', borderRadius: '8px', padding: '3px', border: '1px solid var(--border-subtle)' }}>
            <button
              onClick={() => onSelectZone(undefined)}
              style={{
                background: selectedZoneId === undefined ? 'rgba(16, 185, 129, 0.2)' : 'transparent',
                color: selectedZoneId === undefined ? 'var(--emerald-400)' : 'var(--text-secondary)',
                border: 'none',
                padding: '4px 10px',
                borderRadius: '6px',
                fontSize: '0.78rem',
                cursor: 'pointer',
                fontWeight: 600
              }}
            >
              All Zones
            </button>
            {zones.map(z => (
              <button
                key={z.id}
                onClick={() => onSelectZone(z.id)}
                style={{
                  background: selectedZoneId === z.id ? 'rgba(16, 185, 129, 0.2)' : 'transparent',
                  color: selectedZoneId === z.id ? 'var(--emerald-400)' : 'var(--text-secondary)',
                  border: 'none',
                  padding: '4px 10px',
                  borderRadius: '6px',
                  fontSize: '0.78rem',
                  cursor: 'pointer',
                  fontWeight: 600
                }}
              >
                {z.name.split(' ')[0]}
              </button>
            ))}
          </div>

          {/* Type Filter */}
          <div style={{ display: 'flex', background: 'rgba(255, 255, 255, 0.04)', borderRadius: '8px', padding: '3px', border: '1px solid var(--border-subtle)' }}>
            {(['ALL', 'BIN', 'SOIL', 'LIGHT'] as const).map(type => (
              <button
                key={type}
                onClick={() => setDeviceFilter(type)}
                style={{
                  background: deviceFilter === type ? 'rgba(6, 182, 212, 0.2)' : 'transparent',
                  color: deviceFilter === type ? 'var(--cyan-400)' : 'var(--text-secondary)',
                  border: 'none',
                  padding: '4px 10px',
                  borderRadius: '6px',
                  fontSize: '0.78rem',
                  cursor: 'pointer',
                  fontWeight: 600
                }}
              >
                {type}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* SVG Canvas Container */}
      <div style={{
        position: 'relative',
        width: '100%',
        height: '520px',
        background: '#070b14',
        borderRadius: '12px',
        overflow: 'hidden',
        border: '1px solid rgba(255, 255, 255, 0.06)'
      }}>
        <svg
          viewBox="0 0 900 550"
          style={{ width: '100%', height: '100%', display: 'block' }}
        >
          <defs>
            {/* Grid Pattern */}
            <pattern id="grid" width="30" height="30" patternUnits="userSpaceOnUse">
              <path d="M 30 0 L 0 0 0 30" fill="none" stroke="rgba(255, 255, 255, 0.03)" strokeWidth="1" />
            </pattern>
            {/* Zone Gradients */}
            <linearGradient id="zone1Grad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#10b981" stopOpacity="0.12" />
              <stop offset="100%" stopColor="#06b6d4" stopOpacity="0.04" />
            </linearGradient>
            <linearGradient id="zone2Grad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#8b5cf6" stopOpacity="0.12" />
              <stop offset="100%" stopColor="#ec4899" stopOpacity="0.04" />
            </linearGradient>
            <linearGradient id="zone3Grad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#f59e0b" stopOpacity="0.12" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0.04" />
            </linearGradient>
            <radialGradient id="fountainGrad" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stopColor="#38bdf8" stopOpacity="0.7" />
              <stop offset="100%" stopColor="#0284c7" stopOpacity="0.1" />
            </radialGradient>
          </defs>

          {/* Background Grid */}
          <rect width="900" height="550" fill="url(#grid)" />

          {/* Decorative Lake / Pond in Center-East */}
          <path
            d="M 440,160 C 500,120 620,130 680,180 C 740,240 700,320 620,340 C 530,360 480,290 440,240 Z"
            fill="rgba(14, 165, 233, 0.08)"
            stroke="rgba(56, 189, 248, 0.25)"
            strokeWidth="1.5"
            strokeDasharray="4 2"
          />
          <text x="560" y="240" fill="rgba(56, 189, 248, 0.4)" fontSize="11" fontFamily="var(--font-mono)" letterSpacing="1">
            BOTANICAL LAGOON
          </text>

          {/* ZONE 1: Central Plaza & Promenade */}
          <g style={{ cursor: 'pointer' }} onClick={() => onSelectZone(selectedZoneId === 1 ? undefined : 1)}>
            <polygon
              points="150,80 430,80 410,320 160,320"
              fill="url(#zone1Grad)"
              stroke={selectedZoneId === 1 ? 'var(--emerald-400)' : 'rgba(16, 185, 129, 0.35)'}
              strokeWidth={selectedZoneId === 1 ? 2.5 : 1.5}
            />
            {/* Grand Fountain Ring */}
            <circle cx="290" cy="210" r="38" fill="url(#fountainGrad)" stroke="rgba(56, 189, 248, 0.4)" strokeWidth="1.5" />
            <circle cx="290" cy="210" r="14" fill="#0284c7" opacity="0.6" />
            <text x="175" y="115" fill="var(--emerald-400)" fontSize="12" fontWeight="700" letterSpacing="0.05em">
              ZONE 1 &middot; CENTRAL PROMENADE
            </text>
            <text x="175" y="132" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">
              Health: 94.5% &middot; Paved Plaza
            </text>
          </g>

          {/* ZONE 2: Botanical Gardens & Arboretum */}
          <g style={{ cursor: 'pointer' }} onClick={() => onSelectZone(selectedZoneId === 2 ? undefined : 2)}>
            <polygon
              points="450,90 850,90 830,370 430,370"
              fill="url(#zone2Grad)"
              stroke={selectedZoneId === 2 ? 'var(--violet-400)' : 'rgba(139, 92, 246, 0.35)'}
              strokeWidth={selectedZoneId === 2 ? 2.5 : 1.5}
            />
            {/* Greenhouse Poly */}
            <rect x="650" y="270" width="80" height="50" rx="6" fill="rgba(167, 139, 250, 0.15)" stroke="rgba(167, 139, 250, 0.4)" strokeWidth="1" />
            <text x="660" y="300" fill="var(--violet-400)" fontSize="9" fontFamily="var(--font-mono)">
              GREENHOUSE
            </text>
            <text x="470" y="125" fill="var(--violet-400)" fontSize="12" fontWeight="700" letterSpacing="0.05em">
              ZONE 2 &middot; BOTANICAL ARBORETUM
            </text>
            <text x="470" y="142" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">
              Health: 88.0% &middot; Flora & Deep Soil
            </text>
          </g>

          {/* ZONE 3: Athletics & Sports Complex */}
          <g style={{ cursor: 'pointer' }} onClick={() => onSelectZone(selectedZoneId === 3 ? undefined : 3)}>
            <polygon
              points="140,350 480,350 460,520 140,520"
              fill="url(#zone3Grad)"
              stroke={selectedZoneId === 3 ? 'var(--amber-400)' : 'rgba(245, 158, 11, 0.35)'}
              strokeWidth={selectedZoneId === 3 ? 2.5 : 1.5}
            />
            {/* Running Track Oval */}
            <ellipse cx="300" cy="445" rx="110" ry="45" fill="none" stroke="rgba(245, 158, 11, 0.25)" strokeWidth="3" />
            <ellipse cx="300" cy="445" rx="85" ry="30" fill="rgba(245, 158, 11, 0.05)" stroke="rgba(245, 158, 11, 0.2)" strokeWidth="1" />
            <text x="160" y="380" fill="var(--amber-400)" fontSize="12" fontWeight="700" letterSpacing="0.05em">
              ZONE 3 &middot; ATHLETICS COMPLEX
            </text>
            <text x="160" y="396" fill="var(--text-muted)" fontSize="10" fontFamily="var(--font-mono)">
              Health: 91.2% &middot; Turf & Track
            </text>
          </g>

          {/* Walkways and Paths Connecting Zones */}
          <path
            d="M 290,320 L 290,350 M 410,210 L 450,210 M 430,370 L 430,420"
            stroke="rgba(255, 255, 255, 0.15)"
            strokeWidth="3"
            strokeDasharray="6 4"
          />

          {/* IoT Device Pins */}
          {filteredDevices.map(device => {
            const pinColor = getPinColor(device);
            const isHovered = hoveredDevice?.id === device.id;

            return (
              <g
                key={device.id}
                transform={`translate(${device.x}, ${device.y})`}
                style={{ cursor: 'pointer', transition: 'transform 0.15s ease' }}
                onMouseEnter={() => setHoveredDevice(device)}
                onMouseLeave={() => setHoveredDevice(null)}
                onClick={() => onSelectDevice(device)}
              >
                {/* Glow ring on hover */}
                {isHovered && (
                  <circle
                    r="24"
                    fill="none"
                    stroke={pinColor}
                    strokeWidth="1.5"
                    opacity="0.6"
                    className="pulse-anim"
                  />
                )}

                {/* Base halo */}
                <circle
                  r="14"
                  fill="#0f172a"
                  stroke={pinColor}
                  strokeWidth={isHovered ? '2.5' : '1.5'}
                />

                {/* Status Dot */}
                <circle
                  cx="9"
                  cy="-9"
                  r="4"
                  fill={pinColor}
                />

                {/* Centered Icon */}
                <g transform="translate(-6.5, -6.5)" fill={pinColor} stroke={pinColor}>
                  {getDeviceIcon(device.type)}
                </g>
              </g>
            );
          })}
        </svg>

        {/* Hover Tooltip Overlay */}
        {hoveredDevice && (
          <div style={{
            position: 'absolute',
            left: `${Math.min(720, Math.max(30, hoveredDevice.x - 60))}px`,
            top: `${Math.max(20, hoveredDevice.y - 110)}px`,
            background: 'rgba(15, 23, 42, 0.95)',
            backdropFilter: 'blur(12px)',
            border: '1px solid rgba(255, 255, 255, 0.15)',
            borderRadius: '10px',
            padding: '10px 14px',
            pointerEvents: 'none',
            zIndex: 30,
            boxShadow: '0 8px 24px rgba(0,0,0,0.5)',
            minWidth: '180px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }}>
              <span style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--cyan-400)' }}>
                {hoveredDevice.deviceUid}
              </span>
              <span className={`badge ${hoveredDevice.status === 'ONLINE' ? 'badge-online' : 'badge-warning'}`} style={{ fontSize: '0.65rem', padding: '2px 6px' }}>
                {hoveredDevice.status}
              </span>
            </div>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', marginTop: '3px' }}>
              {hoveredDevice.name}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
              {hoveredDevice.type === 'BIN' && `Fill Level: ${hoveredDevice.metrics?.fillPercentage}%`}
              {hoveredDevice.type === 'SOIL' && `Moisture: ${hoveredDevice.metrics?.soilMoisture}%`}
              {hoveredDevice.type === 'LIGHT' && `Lux: ${hoveredDevice.metrics?.lightLux} lx`}
            </div>
            <div style={{ fontSize: '0.7rem', color: 'var(--emerald-400)', marginTop: '2px' }}>
              Battery: {hoveredDevice.batteryLevel}% &middot; Click to inspect
            </div>
          </div>
        )}

        {/* Map Legend */}
        <div style={{
          position: 'absolute',
          bottom: '12px',
          right: '12px',
          background: 'rgba(15, 23, 42, 0.85)',
          backdropFilter: 'blur(10px)',
          border: '1px solid var(--border-subtle)',
          borderRadius: '8px',
          padding: '8px 14px',
          display: 'flex',
          gap: '16px',
          fontSize: '0.75rem',
          color: 'var(--text-secondary)'
        }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--emerald-400)' }}></span>
            Normal
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--amber-400)' }}></span>
            Warning / Elevated
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--rose-400)' }}></span>
            Critical / Anomaly
          </span>
        </div>
      </div>
    </div>
  );
};
