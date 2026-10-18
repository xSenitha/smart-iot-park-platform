from datetime import datetime, timezone, timedelta
from typing import List, Dict, Any, Tuple
import math

class BinOverflowPredictor:
    def __init__(self, target_capacity: float = 90.0):
        self.target_capacity = target_capacity
        self.model_name = "bin_overflow_ewma_v1"

    def predict(self, readings: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Predict hours remaining until bin reaches target fill capacity (default 90%).
        Readings are expected to be ordered newest first or oldest first.
        """
        if not readings:
            return {
                "currentFill": 0.0,
                "fillRatePerHour": 0.0,
                "hoursUntilFull": 48.0,
                "projectedFullTimestamp": None,
                "riskLevel": "LOW",
                "confidence": 0.3,
                "recommendation": "Insufficient sensor telemetry window"
            }

        # Extract timestamps and fill percentages
        series: List[Tuple[float, float]] = []
        for r in readings:
            metrics = r.get("metrics", {})
            fill = metrics.get("fillPercentage")
            ts_raw = r.get("timestamp")
            if fill is not None and ts_raw is not None:
                try:
                    if isinstance(ts_raw, str):
                        dt = datetime.fromisoformat(ts_raw.replace("Z", "+00:00"))
                    elif isinstance(ts_raw, datetime):
                        dt = ts_raw
                    else:
                        continue
                    series.append((dt.timestamp(), float(fill)))
                except Exception:
                    continue

        if len(series) < 2:
            current = series[0][1] if series else 0.0
            return {
                "currentFill": round(current, 1),
                "fillRatePerHour": 0.0,
                "hoursUntilFull": 24.0,
                "projectedFullTimestamp": None,
                "riskLevel": "LOW" if current < 80 else "HIGH",
                "confidence": 0.5,
                "recommendation": "Single reading available"
            }

        # Sort chronologically ascending
        series.sort(key=lambda x: x[0])
        current_fill = series[-1][1]

        # Calculate slope (rate of change per hour) using linear regression over the window
        n = len(series)
        t_0 = series[0][0]
        # x in hours from start of window
        x = [(pt[0] - t_0) / 3600.0 for pt in series]
        y = [pt[1] for pt in series]

        x_mean = sum(x) / n
        y_mean = sum(y) / n

        denom = sum((xi - x_mean) ** 2 for xi in x)
        if denom > 1e-6:
            slope = sum((x[i] - x_mean) * (y[i] - y_mean) for i in range(n)) / denom
        else:
            slope = 0.0

        # If slope is slightly negative or zero due to noise, default to small positive growth
        if slope <= 0.05:
            # Check if bin was emptied during window
            if y[-1] < y[0] - 20:
                fill_rate = 1.5 # standard baseline fill rate
            else:
                fill_rate = 0.5
        else:
            fill_rate = slope

        # Cap fill rate to realistic municipal bounds (0.1% to 25% per hour)
        fill_rate = max(0.1, min(25.0, round(fill_rate, 2)))

        remaining_capacity = max(0.0, self.target_capacity - current_fill)
        hours_until_full = round(remaining_capacity / fill_rate, 1)

        # Risk stratification
        if current_fill >= 90.0 or hours_until_full <= 2.0:
            risk_level = "CRITICAL"
            recommendation = "Immediate waste collection required within 2 hours"
        elif current_fill >= 75.0 or hours_until_full <= 5.0:
            risk_level = "HIGH"
            recommendation = "Schedule waste collection in upcoming dispatch shift"
        elif current_fill >= 50.0 or hours_until_full <= 12.0:
            risk_level = "MEDIUM"
            recommendation = "Monitor fill level closely during peak afternoon hours"
        else:
            risk_level = "LOW"
            recommendation = "Normal operating parameters"

        projected_dt = datetime.now(timezone.utc) + timedelta(hours=hours_until_full)
        confidence = min(0.98, max(0.65, round(0.70 + (min(n, 50) / 100.0) * 0.25, 2)))

        return {
            "currentFill": round(current_fill, 1),
            "fillRatePerHour": fill_rate,
            "hoursUntilFull": hours_until_full,
            "projectedFullTimestamp": projected_dt.isoformat(),
            "riskLevel": risk_level,
            "confidence": confidence,
            "recommendation": recommendation
        }

bin_predictor = BinOverflowPredictor()
