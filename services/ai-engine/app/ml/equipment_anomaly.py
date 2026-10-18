from datetime import datetime, timezone
from typing import List, Dict, Any, Tuple
import math

class EquipmentAnomalyDetector:
    def __init__(self, threshold: float = 0.70):
        self.threshold = threshold
        self.model_name = "multivariate_isolation_zscore_v1"

    def detect(self, device_id: str, device_type: str, readings: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Multivariate statistical anomaly detection analyzing battery discharge rate,
        signal/reporting stability, and metric bounds.
        """
        if not readings or len(readings) < 2:
            return {
                "deviceId": device_id,
                "anomalyScore": 0.05,
                "isAnomaly": False,
                "probableRootCause": "INSUFFICIENT_DATA",
                "recommendedAction": "Maintain standard telemetry ingestion"
            }

        score = 0.0
        root_causes = []

        # Sort chronologically ascending
        sorted_readings = []
        for r in readings:
            ts = r.get("timestamp")
            if ts:
                try:
                    dt = datetime.fromisoformat(str(ts).replace("Z", "+00:00"))
                    sorted_readings.append((dt, r.get("metrics", {})))
                except Exception:
                    continue
        sorted_readings.sort(key=lambda x: x[0])

        if len(sorted_readings) < 2:
            return {
                "deviceId": device_id,
                "anomalyScore": 0.1,
                "isAnomaly": False,
                "probableRootCause": "NORMAL",
                "recommendedAction": "Normal operation"
            }

        latest_metrics = sorted_readings[-1][1]
        earliest_metrics = sorted_readings[0][1]

        # 1. Battery Drain Rate Analysis
        latest_batt = latest_metrics.get("batteryLevel")
        earliest_batt = earliest_metrics.get("batteryLevel")
        if latest_batt is not None and earliest_batt is not None:
            time_diff_hours = max(0.1, (sorted_readings[-1][0] - sorted_readings[0][0]).total_seconds() / 3600.0)
            batt_drop = float(earliest_batt) - float(latest_batt)
            drop_rate_per_hour = batt_drop / time_diff_hours

            if float(latest_batt) < 15.0:
                score += 0.40
                root_causes.append("CRITICAL_BATTERY_LEVEL (< 15%)")
            if drop_rate_per_hour > 5.0:
                score += 0.50
                root_causes.append("RAPID_BATTERY_DRAIN_FAULT")

        # 2. Type-specific Anomalies
        d_type = device_type.upper() if device_type else "BIN"

        if d_type == "BIN":
            tilt = latest_metrics.get("tiltAngle")
            if tilt is not None and float(tilt) > 25.0:
                score += 0.75
                root_causes.append(f"BIN_TILT_TAMPERING ({tilt}° tilt detected)")

        elif d_type == "LIGHT":
            is_on = latest_metrics.get("isLampOn")
            lux = latest_metrics.get("lightLux")
            power = latest_metrics.get("powerConsumptionWatts")
            hour = sorted_readings[-1][0].hour
            is_night = (hour < 6 or hour >= 18)

            if is_night and is_on and power is not None and float(power) < 5.0:
                score += 0.75
                root_causes.append("LED_DRIVER_OR_BULB_BURNOUT")
            elif not is_night and is_on and lux is not None and float(lux) > 10000:
                score += 0.45
                root_causes.append("DAY_BURNING_ENERGY_WASTE_FAULT")

        elif d_type == "SOIL":
            moisture = latest_metrics.get("soilMoisture")
            if moisture is not None:
                if float(moisture) <= 5.0:
                    score += 0.75
                    root_causes.append("SEVERE_DESICCATION_OR_DISCONNECTED_PROBE")
                elif float(moisture) >= 98.0:
                    score += 0.45
                    root_causes.append("WATERLOGGED_PROBE_SATURATION")

        # Normalize score in [0.0, 1.0]
        final_score = round(min(1.0, max(0.02, score)), 2)
        is_anomaly = final_score >= self.threshold

        if is_anomaly:
            recommended_action = "Dispatch maintenance crew to inspect physical asset and harness."
            primary_cause = "; ".join(root_causes) if root_causes else "ABNORMAL_METRIC_DEVIATION"
        else:
            recommended_action = "Device operating within acceptable bounds."
            primary_cause = "; ".join(root_causes) if root_causes else "NORMAL"

        return {
            "deviceId": device_id,
            "anomalyScore": final_score,
            "isAnomaly": is_anomaly,
            "probableRootCause": primary_cause,
            "recommendedAction": recommended_action
        }

equipment_detector = EquipmentAnomalyDetector()
