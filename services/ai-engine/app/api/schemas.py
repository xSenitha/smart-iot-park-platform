"""
Pydantic Schemas for AI Engine Request and Response contracts.
"""
from typing import List, Dict, Any, Optional
from datetime import datetime
from app.core.compat import BaseModel, Field

# ----------------- Overflow Forecasting -----------------
class OverflowPredictionRequest(BaseModel):
    deviceId: Optional[str] = Field(default=None, description="Device UID if pulling telemetry from Mongo")
    readings: Optional[List[Dict[str, Any]]] = Field(default=None, description="Ad-hoc sensor readings to evaluate")

class OverflowPredictionResponse(BaseModel):
    currentFill: float = Field(..., description="Current fill level percentage")
    fillRatePerHour: float = Field(..., description="Projected fill increase rate per hour")
    hoursUntilFull: float = Field(..., description="Hours remaining until target capacity is reached")
    projectedFullTimestamp: Optional[str] = Field(default=None, description="ISO timestamp of predicted overflow")
    riskLevel: str = Field(..., description="LOW, MEDIUM, HIGH, or CRITICAL")
    confidence: float = Field(..., description="Model statistical confidence score [0.0 - 1.0]")
    recommendation: str = Field(..., description="Operational advice for municipal waste crew")

# ----------------- Hardware Anomaly Detection -----------------
class AnomalyDetectionRequest(BaseModel):
    deviceId: str = Field(..., description="Device UID")
    deviceType: Optional[str] = Field(default="BIN", description="BIN, LIGHT, or SOIL")
    readings: Optional[List[Dict[str, Any]]] = Field(default=None, description="Recent telemetry readings")

class AnomalyDetectionResponse(BaseModel):
    deviceId: str = Field(..., description="Device UID")
    anomalyScore: float = Field(..., description="Normalized anomaly score [0.0 - 1.0]")
    isAnomaly: bool = Field(..., description="True if score exceeds critical threshold")
    probableRootCause: str = Field(..., description="Identified root cause or failure pattern")
    recommendedAction: str = Field(..., description="Corrective action for dispatch")

# ----------------- Irrigation Need Scoring -----------------
class IrrigationScoringRequest(BaseModel):
    zoneId: int = Field(..., description="Target Zone ID")
    targetMoisture: float = Field(default=40.0, description="Botanical target soil moisture percentage")
    readings: Optional[List[Dict[str, Any]]] = Field(default=None, description="Recent zone soil readings")

class IrrigationScoringResponse(BaseModel):
    zoneId: int = Field(..., description="Target Zone ID")
    currentMoisture: float = Field(..., description="Current mean soil moisture level")
    targetMoisture: float = Field(..., description="Target soil moisture level")
    moistureDeficit: float = Field(..., description="Moisture deficit percentage")
    urgency: str = Field(..., description="LOW, MODERATE, HIGH, or CRITICAL")
    recommendedWateringMinutes: int = Field(..., description="Recommended irrigation runtime in minutes")
    recommendedWindow: str = Field(..., description="Recommended time window for water delivery")
    summary: str = Field(..., description="Agronomic health summary")

# ----------------- Batch Job Execution -----------------
class BatchJobResponse(BaseModel):
    status: str = Field(..., description="Execution status: COMPLETED or FAILED")
    devicesEvaluated: int = Field(..., description="Number of IoT assets evaluated")
    predictionsCreated: int = Field(..., description="Number of inference records written to MongoDB")
    alertsTriggered: int = Field(..., description="Number of alerts posted to Core API")
    ticketsCreated: int = Field(..., description="Number of maintenance tickets created")
    timestamp: str = Field(..., description="Execution finish ISO timestamp")

# ----------------- Health & Status -----------------
class HealthResponse(BaseModel):
    status: str = Field(..., description="UP or DOWN")
    service: str = Field(default="ai-engine", description="Service identifier")
    version: str = Field(..., description="Service version")
    timestamp: str = Field(..., description="Current UTC timestamp")
    mongoConnected: bool = Field(..., description="MongoDB connectivity status")
