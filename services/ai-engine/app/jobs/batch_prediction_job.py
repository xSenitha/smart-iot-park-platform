import logging
from datetime import datetime, timezone
from typing import Dict, Any
from app.core.config import settings
from app.db.mongo import mongo_manager
from app.clients.core_api_client import core_api_client
from app.ml.bin_overflow import bin_predictor
from app.ml.equipment_anomaly import equipment_detector
from app.ml.irrigation_need import irrigation_scorer

logger = logging.getLogger("ai_engine.jobs")

def run_batch_prediction_cycle() -> Dict[str, Any]:
    """
    Execute full inference pipeline across all active devices in all park zones:
    1. Read sensor history from MongoDB
    2. Compute overflow forecasts and anomaly scores
    3. Persist predictions into MongoDB
    4. Write alerts and maintenance tickets back to Core API
    """
    logger.info("Starting scheduled AI predictive maintenance batch inference cycle...")
    
    devices = core_api_client.get_devices()
    if not devices:
        logger.debug("Core API devices endpoint returned empty. Using baseline device UIDs.")
        devices = [
            {"id": 1, "deviceUid": "DEV-BIN-0001", "name": "Plaza North Bin", "type": "BIN", "zoneId": 1, "parkId": 1},
            {"id": 2, "deviceUid": "DEV-BIN-0002", "name": "Plaza Fountain Bin", "type": "BIN", "zoneId": 1, "parkId": 1},
            {"id": 3, "deviceUid": "DEV-BIN-0003", "name": "Rose Garden Bin", "type": "BIN", "zoneId": 2, "parkId": 1},
            {"id": 4, "deviceUid": "DEV-BIN-0004", "name": "Greenhouse Bin", "type": "BIN", "zoneId": 2, "parkId": 1},
            {"id": 5, "deviceUid": "DEV-BIN-0005", "name": "Sports Bleachers Bin", "type": "BIN", "zoneId": 3, "parkId": 1},
            {"id": 6, "deviceUid": "DEV-BIN-0006", "name": "Sports Pavilion Bin", "type": "BIN", "zoneId": 3, "parkId": 1},
            {"id": 7, "deviceUid": "DEV-SOIL-0001", "name": "Central Lawn Sensor", "type": "SOIL", "zoneId": 1, "parkId": 1},
            {"id": 8, "deviceUid": "DEV-SOIL-0002", "name": "Botanical Orchid Probe", "type": "SOIL", "zoneId": 2, "parkId": 1},
            {"id": 12, "deviceUid": "DEV-LIGHT-0001", "name": "Plaza Luminaire", "type": "LIGHT", "zoneId": 1, "parkId": 1},
        ]

    predictions_created = 0
    alerts_triggered = 0
    tickets_created = 0

    for dev in devices:
        d_id = dev.get("id", 1)
        uid = dev.get("deviceUid")
        d_type = dev.get("type", "BIN")
        z_id = dev.get("zoneId", 1)
        p_id = dev.get("parkId", 1)

        readings = mongo_manager.get_recent_readings(uid, limit=50)

        # 1. Bin Overflow Forecast
        if d_type == "BIN":
            forecast = bin_predictor.predict(readings)
            pred_doc = {
                "predictionType": "BIN_OVERFLOW",
                "deviceId": uid,
                "zoneId": z_id,
                "parkId": p_id,
                "metrics": forecast,
                "modelVersion": bin_predictor.model_name,
                "generatedAt": datetime.now(timezone.utc).isoformat()
            }
            mongo_manager.save_prediction(pred_doc)
            predictions_created += 1

            # Threshold trigger: overflow within 3 hours or critical fill
            if forecast.get("hoursUntilFull", 99.0) <= settings.BIN_OVERFLOW_WARNING_HOURS or forecast.get("riskLevel") in ("HIGH", "CRITICAL"):
                alerts_triggered += 1
                core_api_client.create_alert(
                    device_id=d_id,
                    alert_type="AI_PREDICTED_OVERFLOW",
                    severity="CRITICAL" if forecast["riskLevel"] == "CRITICAL" else "WARNING",
                    title=f"AI Forecast: {dev.get('name', uid)} overflow in {forecast['hoursUntilFull']}h",
                    message=f"Current fill: {forecast['currentFill']}%. Fill rate: {forecast['fillRatePerHour']}%/h. {forecast['recommendation']}"
                )

                if forecast["riskLevel"] == "CRITICAL":
                    tickets_created += 1
                    core_api_client.create_maintenance_ticket(
                        park_id=p_id,
                        zone_id=z_id,
                        device_id=d_id,
                        title=f"Urgent Bin Emptying: {dev.get('name', uid)}",
                        description=f"AI model predicted overflow at {forecast.get('projectedFullTimestamp')}. Dispatch collection crew.",
                        priority="URGENT"
                    )

        # 2. Equipment Anomaly Detection
        anomaly = equipment_detector.detect(uid, d_type, readings)
        if anomaly["anomalyScore"] > 0.2:
            pred_doc = {
                "predictionType": "EQUIPMENT_ANOMALY",
                "deviceId": uid,
                "zoneId": z_id,
                "parkId": p_id,
                "metrics": anomaly,
                "modelVersion": equipment_detector.model_name,
                "generatedAt": datetime.now(timezone.utc).isoformat()
            }
            mongo_manager.save_prediction(pred_doc)
            predictions_created += 1

        if anomaly["isAnomaly"]:
            alerts_triggered += 1
            core_api_client.create_alert(
                device_id=d_id,
                alert_type="AI_EQUIPMENT_ANOMALY",
                severity="CRITICAL" if anomaly["anomalyScore"] >= 0.85 else "WARNING",
                title=f"Hardware Anomaly: {dev.get('name', uid)} ({anomaly['probableRootCause']})",
                message=f"Anomaly score: {anomaly['anomalyScore']}. {anomaly['recommendedAction']}"
            )
            tickets_created += 1
            core_api_client.create_maintenance_ticket(
                park_id=p_id,
                zone_id=z_id,
                device_id=d_id,
                title=f"Asset Inspection: {dev.get('name', uid)}",
                description=f"Anomaly detected with score {anomaly['anomalyScore']}. Root cause: {anomaly['probableRootCause']}.",
                priority="HIGH"
            )

    logger.info("Batch inference completed: %d predictions saved, %d alerts triggered, %d tickets dispatched.",
                predictions_created, alerts_triggered, tickets_created)

    return {
        "status": "COMPLETED",
        "devicesEvaluated": len(devices),
        "predictionsCreated": predictions_created,
        "alertsTriggered": alerts_triggered,
        "ticketsCreated": tickets_created,
        "timestamp": datetime.now(timezone.utc).isoformat()
    }
