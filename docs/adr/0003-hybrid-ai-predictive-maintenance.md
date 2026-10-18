# ADR 0003: Statistical Machine Learning vs Deep Learning for Edge Analytics

## Status
Accepted

## Context
Municipal IoT predictive maintenance requires high reliability, mathematical interpretability (root cause isolation), and low CPU inference latency. Heavy deep learning models (e.g. LSTM/Transformers) introduce high memory footprints, inference latency, and "black-box" decision opacity that hinders field technicians.

## Decision
We implemented a **domain-optimized statistical ML suite** in Python 3.12 (FastAPI):
1. **Bin Overflow Forecasting**: Ordinary Least Squares (OLS) slope regression combined with Exponentially Weighted Moving Average (EWMA) to forecast hours until 90% full capacity.
2. **Hardware Anomaly Detection**: Multivariate statistical z-score and deviation analysis evaluating battery drop velocity, solar photocell day-burning faults, luminaire bulb burnout, and probe desiccation.
3. **Botanical Irrigation Optimization**: Soil water retention deficit analysis computing optimal twilight watering durations in minutes.

## Consequences
- **Positive**:
  - Sub-5ms inference latency per device.
  - Transparent, explainable root causes provided directly to maintenance crew tickets.
  - Zero GPU requirement in production cloud deployment.
- **Negative**:
  - Requires domain-specific parameter calibration across distinct seasonal weather cycles.
