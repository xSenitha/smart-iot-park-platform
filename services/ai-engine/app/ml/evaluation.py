from datetime import datetime, timezone
from typing import List, Dict, Any

def get_model_registry() -> List[Dict[str, Any]]:
    return [
        {
            "modelName": "bin_overflow_forecaster",
            "modelVersion": "v1.2.0",
            "algorithm": "Ordinary Least Squares + Exponential Weighted Trend",
            "lastTrained": "2026-10-18T00:00:00Z",
            "sampleCount": 12500,
            "metrics": {
                "maeHours": 0.38,
                "rmseHours": 0.54,
                "r2Score": 0.91
            },
            "features": ["fill_velocity_1h", "fill_velocity_6h", "crowd_diurnal_factor"],
            "status": "DEPLOYED"
        },
        {
            "modelName": "equipment_anomaly_detector",
            "modelVersion": "v1.0.4",
            "algorithm": "Multivariate Z-Score + Mahalanobis Distance",
            "lastTrained": "2026-10-18T00:00:00Z",
            "sampleCount": 8400,
            "metrics": {
                "precision": 0.94,
                "recall": 0.89,
                "f1Score": 0.91
            },
            "features": ["battery_discharge_rate", "photocell_lux_deviation", "tilt_angle"],
            "status": "DEPLOYED"
        },
        {
            "modelName": "irrigation_need_scorer",
            "modelVersion": "v1.1.0",
            "algorithm": "Soil Water Retention & Evapotranspiration Deficit Optimizer",
            "lastTrained": "2026-10-18T00:00:00Z",
            "sampleCount": 9600,
            "metrics": {
                "accuracy": 0.96,
                "meanMoistureError": 1.2
            },
            "features": ["soil_moisture", "evaporation_sun_intensity", "target_threshold"],
            "status": "DEPLOYED"
        }
    ]
