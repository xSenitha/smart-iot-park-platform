import React, { useState } from 'react';
import type { Device } from '../types';
import { TrendingUp, BarChart2, Zap, Droplets } from 'lucide-react';

interface TelemetryChartsProps {
  devices: Device[];
}

export const TelemetryCharts: React.FC<TelemetryChartsProps> = ({ devices }) => {
  const [activeMetricTab, setActiveMetricTab] = useState<'BIN' | 'SOIL' | 'LIGHT'>('BIN');

  // Synthetic 24-hour time series baseline matching municipal patterns
  const hours = ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'];

  // Bin Fill Curve (slow morning, steep afternoon traffic)
  const binSeries = [
    { name: 'DEV-BIN-0002 (Plaza Fountain)', values: [15, 18, 20, 22, 35, 48, 62, 84, 88, 92, 94, 95], color: 'var(--rose-400)' },
    { name: 'DEV-BIN-0004 (Greenhouse)', values: [20, 22, 23, 25, 38, 52, 68, 88, 89, 90, 91, 91], color: 'var(--amber-400)' },
    { name: 'DEV-BIN-0001 (Plaza North)', values: [10, 12, 14, 16, 25, 35, 45, 58, 64, 68, 70, 71], color: 'var(--emerald-400)' }
  ];

  // Soil Moisture Curve (drying during hot afternoon, dipping towards 38%)
  const soilSeries = [
    { name: 'DEV-SOIL-0005 (Main Pitch Turf)', values: [46, 45, 44, 43, 41, 39, 38, 38, 37, 39, 42, 45], color: 'var(--amber-400)' },
    { name: 'DEV-SOIL-0001 (Central Lawn)', values: [48, 47, 46, 45, 44, 43, 42, 42, 43, 45, 47, 48], color: 'var(--emerald-400)' },
    { name: 'DEV-SOIL-0003 (Botanical Orchid)', values: [55, 54, 53, 52, 50, 49, 48, 48, 49, 51, 53, 54], color: 'var(--cyan-400)' }
  ];

  // Lighting Lux vs Watts Curve (lux peaks noon, lamp watts turns on dusk 18:00)
  const lightSeries = [
    { name: 'Ambient Lux / 500 (DEV-LIGHT-0001)', values: [0, 0, 0, 15, 45, 85, 95, 90, 60, 5, 0, 0], color: 'var(--amber-400)' },
    { name: 'Lamp Power Watts (DEV-LIGHT-0001)', values: [60, 60, 60, 0, 0, 0, 0, 0, 0, 60, 60, 60], color: 'var(--cyan-400)' }
  ];

  const currentSeries = activeMetricTab === 'BIN' ? binSeries : activeMetricTab === 'SOIL' ? soilSeries : lightSeries;

  // Chart rendering coordinates
  const svgWidth = 800;
  const svgHeight = 260;
  const paddingX = 50;
  const paddingY = 40;
  const plotWidth = svgWidth - paddingX * 2;
  const plotHeight = svgHeight - paddingY * 2;

  const getPoints = (values: number[]) => {
    return values.map((val, idx) => {
      const x = paddingX + (idx / (values.length - 1)) * plotWidth;
      const y = paddingY + plotHeight - (val / 100) * plotHeight;
      return `${x},${y}`;
    }).join(' ');
  };

  return (
    <div className="glass-card" style={{ padding: '24px', margin: '0 24px 24px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <TrendingUp size={20} color="var(--cyan-400)" />
          <div>
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Telemetry Stream & Time-Series Analytics</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              24-Hour continuous multi-sensor telemetry regression window ({devices.length} reporting nodes)
            </p>
          </div>
        </div>

        {/* Tab Selector */}
        <div style={{ display: 'flex', background: 'rgba(255, 255, 255, 0.04)', borderRadius: '8px', padding: '3px', border: '1px solid var(--border-subtle)' }}>
          <button
            onClick={() => setActiveMetricTab('BIN')}
            style={{
              background: activeMetricTab === 'BIN' ? 'rgba(244, 63, 94, 0.2)' : 'transparent',
              color: activeMetricTab === 'BIN' ? 'var(--rose-400)' : 'var(--text-secondary)',
              border: 'none',
              padding: '6px 14px',
              borderRadius: '6px',
              fontSize: '0.8rem',
              cursor: 'pointer',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <BarChart2 size={14} /> Waste Fill %
          </button>
          <button
            onClick={() => setActiveMetricTab('SOIL')}
            style={{
              background: activeMetricTab === 'SOIL' ? 'rgba(16, 185, 129, 0.2)' : 'transparent',
              color: activeMetricTab === 'SOIL' ? 'var(--emerald-400)' : 'var(--text-secondary)',
              border: 'none',
              padding: '6px 14px',
              borderRadius: '6px',
              fontSize: '0.8rem',
              cursor: 'pointer',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <Droplets size={14} /> Soil Moisture %
          </button>
          <button
            onClick={() => setActiveMetricTab('LIGHT')}
            style={{
              background: activeMetricTab === 'LIGHT' ? 'rgba(245, 158, 11, 0.2)' : 'transparent',
              color: activeMetricTab === 'LIGHT' ? 'var(--amber-400)' : 'var(--text-secondary)',
              border: 'none',
              padding: '6px 14px',
              borderRadius: '6px',
              fontSize: '0.8rem',
              cursor: 'pointer',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <Zap size={14} /> Lux & Power
          </button>
        </div>
      </div>

      {/* SVG Chart Container */}
      <div style={{
        background: '#070b14',
        borderRadius: '12px',
        padding: '16px',
        border: '1px solid rgba(255, 255, 255, 0.06)',
        position: 'relative'
      }}>
        <svg viewBox={`0 0 ${svgWidth} ${svgHeight}`} style={{ width: '100%', height: 'auto', display: 'block' }}>
          {/* Horizontal Gridlines */}
          {[0, 25, 50, 75, 100].map(level => {
            const y = paddingY + plotHeight - (level / 100) * plotHeight;
            return (
              <g key={level}>
                <line
                  x1={paddingX}
                  y1={y}
                  x2={svgWidth - paddingX}
                  y2={y}
                  stroke="rgba(255, 255, 255, 0.07)"
                  strokeWidth="1"
                  strokeDasharray="4 4"
                />
                <text
                  x={paddingX - 10}
                  y={y + 4}
                  fill="var(--text-muted)"
                  fontSize="10"
                  textAnchor="end"
                  fontFamily="var(--font-mono)"
                >
                  {level}%
                </text>
              </g>
            );
          })}

          {/* Critical Threshold Danger Zone for Bins */}
          {activeMetricTab === 'BIN' && (
            <g>
              <line
                x1={paddingX}
                y1={paddingY + plotHeight - (90 / 100) * plotHeight}
                x2={svgWidth - paddingX}
                y2={paddingY + plotHeight - (90 / 100) * plotHeight}
                stroke="var(--rose-500)"
                strokeWidth="1.5"
                strokeDasharray="3 3"
              />
              <text
                x={svgWidth - paddingX - 8}
                y={paddingY + plotHeight - (90 / 100) * plotHeight - 6}
                fill="var(--rose-400)"
                fontSize="10"
                textAnchor="end"
                fontWeight="600"
                fontFamily="var(--font-mono)"
              >
                90% OVERFLOW THRESHOLD
              </text>
            </g>
          )}

          {/* Time Axis Labels */}
          {hours.map((hr, idx) => {
            const x = paddingX + (idx / (hours.length - 1)) * plotWidth;
            return (
              <text
                key={hr}
                x={x}
                y={svgHeight - 12}
                fill="var(--text-muted)"
                fontSize="10"
                textAnchor="middle"
                fontFamily="var(--font-mono)"
              >
                {hr}
              </text>
            );
          })}

          {/* Data Lines */}
          {currentSeries.map(s => {
            const pointsStr = getPoints(s.values);
            return (
              <g key={s.name}>
                <polyline
                  fill="none"
                  stroke={s.color}
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  points={pointsStr}
                />
                {/* Data Points */}
                {s.values.map((val, idx) => {
                  const x = paddingX + (idx / (s.values.length - 1)) * plotWidth;
                  const y = paddingY + plotHeight - (val / 100) * plotHeight;
                  return (
                    <circle
                      key={idx}
                      cx={x}
                      cy={y}
                      r="3.5"
                      fill="#0f172a"
                      stroke={s.color}
                      strokeWidth="2"
                    />
                  );
                })}
              </g>
            );
          })}
        </svg>

        {/* Legend */}
        <div style={{ display: 'flex', gap: '24px', justifyContent: 'center', marginTop: '16px', flexWrap: 'wrap' }}>
          {currentSeries.map(s => (
            <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              <span style={{ width: '12px', height: '3px', background: s.color, borderRadius: '2px' }}></span>
              <span>{s.name}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
