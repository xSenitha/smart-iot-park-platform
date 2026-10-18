from datetime import datetime, timezone
from typing import List, Dict, Any

class IrrigationNeedScorer:
    def __init__(self):
        self.model_name = "irrigation_deficit_optimizer_v1"

    def score_zone(
        self,
        zone_id: int,
        target_moisture: float,
        recent_readings: List[Dict[str, Any]]
    ) -> Dict[str, Any]:
        """
        Evaluate soil moisture levels in a zone against botanical targets,
        computing moisture deficit, urgency score, and recommended watering duration.
        """
        if not recent_readings:
            return {
                "zoneId": zone_id,
                "currentMoisture": round(target_moisture, 1),
                "targetMoisture": round(target_moisture, 1),
                "moistureDeficit": 0.0,
                "urgency": "LOW",
                "recommendedWateringMinutes": 0,
                "recommendedWindow": "22:00 - 23:30 UTC",
                "summary": "No soil sensors reporting in this zone"
            }

        # Average moisture across the most recent readings for this zone
        moisture_values = []
        for r in recent_readings[:30]:
            metrics = r.get("metrics", {})
            m = metrics.get("soilMoisture")
            if m is not None:
                moisture_values.append(float(m))

        if not moisture_values:
            current_moisture = target_moisture
        else:
            current_moisture = sum(moisture_values) / len(moisture_values)

        current_moisture = round(current_moisture, 1)
        target = round(target_moisture, 1)
        deficit = round(max(0.0, target - current_moisture), 1)

        # Categorize urgency and compute watering duration
        if deficit >= 20.0 or current_moisture <= 18.0:
            urgency = "CRITICAL"
            watering_mins = 45
            summary = "Severe root zone moisture deficit. Immediate irrigation required."
        elif deficit >= 12.0:
            urgency = "HIGH"
            watering_mins = 30
            summary = "Moderate moisture deficit. Run scheduled evening irrigation cycle."
        elif deficit >= 5.0:
            urgency = "MODERATE"
            watering_mins = 15
            summary = "Slight dry-down observed. Light supplemental watering recommended."
        else:
            urgency = "LOW"
            watering_mins = 0
            summary = "Soil moisture is optimal and within acceptable range."

        return {
            "zoneId": zone_id,
            "currentMoisture": current_moisture,
            "targetMoisture": target,
            "moistureDeficit": deficit,
            "urgency": urgency,
            "recommendedWateringMinutes": watering_mins,
            "recommendedWindow": "21:30 - 23:00 UTC",
            "summary": summary
        }

irrigation_scorer = IrrigationNeedScorer()
