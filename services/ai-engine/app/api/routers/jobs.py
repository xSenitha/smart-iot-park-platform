"""
Batch Jobs Router for manual and scheduled AI inference pipeline execution.
"""
from typing import Dict, Any
from app.core.compat import APIRouter
from app.api.schemas import BatchJobResponse
from app.jobs.batch_prediction_job import run_batch_prediction_cycle
from app.core.config import settings

router = APIRouter(prefix="/jobs", tags=["Batch Jobs"])

_last_job_run: Dict[str, Any] = {
    "status": "INITIALIZED",
    "intervalMinutes": settings.AI_PREDICTION_CRON_INTERVAL_MINUTES,
    "lastRun": None,
    "lastResult": None
}

@router.post("/predict-batch", response_model=BatchJobResponse)
def trigger_batch_prediction_job() -> Dict[str, Any]:
    """
    Manually trigger an ad-hoc batch inference cycle across all devices.
    """
    result = run_batch_prediction_cycle()
    _last_job_run["status"] = "COMPLETED"
    _last_job_run["lastRun"] = result.get("timestamp")
    _last_job_run["lastResult"] = result
    return result

@router.get("/status")
def get_job_status() -> Dict[str, Any]:
    """
    Get the operational status of the periodic prediction job.
    """
    return _last_job_run
