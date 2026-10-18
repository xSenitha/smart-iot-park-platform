import '@testing-library/jest-dom/vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('Smart IoT Park Platform Dashboard', () => {
  it('renders application title and key health indicators', () => {
    render(<App />);
    expect(screen.getByText('Smart IoT Park Platform')).toBeInTheDocument();
    expect(screen.getByText('Live Grid Active')).toBeInTheDocument();
    expect(screen.getByText('Park Health Index')).toBeInTheDocument();
  });

  it('navigates between dashboard tabs', () => {
    render(<App />);

    // Click on Analytics Tab
    const analyticsTab = screen.getByRole('button', { name: /Telemetry & Curves/i });
    fireEvent.click(analyticsTab);
    expect(screen.getByText(/Telemetry Stream & Time-Series Analytics/i)).toBeInTheDocument();

    // Click on Alert Center Tab
    const alertsTab = screen.getByRole('button', { name: /Alert Center/i });
    fireEvent.click(alertsTab);
    expect(screen.getByText(/Incident Alert Center/i)).toBeInTheDocument();

    // Click on Maintenance Kanban Tab
    const kanbanTab = screen.getByRole('button', { name: /Maintenance Kanban/i });
    fireEvent.click(kanbanTab);
    expect(screen.getByText(/Maintenance Dispatch & Work Orders Kanban/i)).toBeInTheDocument();

    // Click on AI Predictive Insights Tab
    const aiTab = screen.getByRole('button', { name: /AI Predictive Insights/i });
    fireEvent.click(aiTab);
    expect(screen.getByText(/AI Predictive Intelligence & Maintenance/i)).toBeInTheDocument();
  });

  it('allows switching municipal roles in header', () => {
    render(<App />);
    const roleSelect = screen.getByLabelText(/Role:/i) as HTMLSelectElement;
    expect(roleSelect.value).toBe('ROLE_CITY_PLANNER');

    fireEvent.change(roleSelect, { target: { value: 'ROLE_ADMIN' } });
    expect(roleSelect.value).toBe('ROLE_ADMIN');
  });
});
