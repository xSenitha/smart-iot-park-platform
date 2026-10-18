"""
Predictions Router providing real-time inference endpoints and stored prediction queries.
"""
from typing import List, Dict, Any, Optional
from app.core.compat import APIRouter, HTTPException
from app.api.schemas import (
    OverflowPredictionRequest,
    OverflowPredictionResponse,
    AnomalyDetectionRequest,
    AnomalyDetectionResponse,
    IrrigationScoringRequest,
    IrrigationScoringResponse,
)
from app.ml.bin_overflow import bin_predictor
from app.ml.equipment_anomaly import equipment_detector
from app.ml.irrigation_need import irrigation_scorer
from app.db.mongo import mongo_manager

router = APIRouter(prefix="/predict", tags=["Predictions"])

@router.post("/bin-overflow", response_model=OverflowPredictionResponse)
def predict_bin_overflow(request: OverflowPredictionRequest) -> Dict[str, Any]:
    """
    Predict hours remaining until bin reaches overflow capacity.
    Accepts either an explicit array of telemetry readings, or a deviceId
    to retrieve recent telemetry from MongoDB.
    """
    readings = request.readings
    if not readings and request.deviceId:
        readings = mongo_manager.get_recent_readings(request.deviceId, limit=50)

    result = bin_predictor.predict(readings or [])
    return result

@router.post("/anomaly", response_model=AnomalyDetectionResponse)
def detect_hardware_anomaly(request: AnomalyDetectionRequest) -> Dict[str, Any]:
    """
    Multivariate statistical anomaly detection analyzing battery discharge rate,
    signal/reporting stability, and metric bounds.
    """
    readings = request.readings
    if not readings:
        readings = mongo_manager.get_recent_readings(request.deviceId, limit=50)

    result = equipment_detector.detect(
        device_id=request.deviceId,
        device_type=request.deviceType or "BIN",
        readings=readings or []
    )
    return result

@router.post("/irrigation", response_model=IrrigationScoringResponse)
def score_irrigation_need(request: IrrigationScoringRequest) -> Dict[str, Any]:
    """
    Evaluate zone soil moisture deficit and determine recommended watering schedule.
    """
    readings = request.readings
    if not readings:
        readings = mongo_manager.get_zone_readings(request.zoneId, device_type="SOIL", limit=50)

    result = irrigation_scorer.score_zone(
        zone_id=request.zoneId,
        target_moisture=request.targetMoisture,
        recent_readings=readings or []
    )
    return result

query_router = APIRouter(prefix="/predictions", tags=["Predictions Query"])

@query_router.get("/latest")
def get_latest_predictions(limit: int = 50) -> List[Dict[str, Any]]:
    """
    Retrieve recently calculated predictions from MongoDB.
    """
    return mongo_manager.get_latest_predictions(limit=limit)

@query_router.get("/device/{device_id}")
def get_device_predictions(device_id: str, limit: int = 50) -> List[Dict[str, Any]]:
    """
    Retrieve historical predictions for a specific IoT device.
    """
    return mongo_manager.get_device_predictions(device_id=device_id, limit=limit)
