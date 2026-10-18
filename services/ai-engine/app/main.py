"""
FastAPI Entry Point for Smart IoT Park Platform AI Engine.
Provides REST APIs for predictive maintenance, anomaly detection, and scheduled inference jobs.
"""
import os
import sys
import threading
import time
import logging
from datetime import datetime, timezone
from contextlib import asynccontextmanager
from typing import Dict, Any

from app.core.config import settings
from app.core.logging import setup_logging
from app.core.compat import FastAPI, CORSMiddleware, JSONResponse
from app.api.schemas import HealthResponse
from app.api.routers import predictions, jobs, models
from app.jobs.batch_prediction_job import run_batch_prediction_cycle
from app.db.mongo import mongo_manager

setup_logging()
logger = logging.getLogger("ai_engine.main")

_scheduler_running = False
_scheduler_thread = None

def _background_prediction_worker():
    """Periodic worker executing batch inference according to configured interval."""
    interval = settings.AI_PREDICTION_CRON_INTERVAL_MINUTES * 60
    logger.info("AI Prediction scheduler worker started (Interval: %ds)", interval)
    # Delay initial run slightly to allow other services to settle
    time.sleep(5)
    while _scheduler_running:
        try:
            run_batch_prediction_cycle()
        except Exception as e:
            logger.error("Error in scheduled batch prediction worker: %s", e)
        
        # Sleep in small slices to respond promptly to shutdown
        for _ in range(max(1, interval // 2)):
            if not _scheduler_running:
                break
            time.sleep(2)

@asynccontextmanager
async def lifespan(app: FastAPI):
    global _scheduler_running, _scheduler_thread
    logger.info("Initializing Smart IoT Park AI Engine v%s...", settings.VERSION)
    
    # Verify MongoDB connection
    mongo_manager.get_database()

    # Start background prediction cycle
    _scheduler_running = True
    _scheduler_thread = threading.Thread(target=_background_prediction_worker, daemon=True, name="BatchPredictionWorker")
    _scheduler_thread.start()

    yield

    logger.info("Shutting down AI Engine background scheduler...")
    _scheduler_running = False
    if _scheduler_thread and _scheduler_thread.is_alive():
        _scheduler_thread.join(timeout=3)

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    lifespan=lifespan,
    docs_url="/docs"
)

# CORS configuration
try:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
except Exception:
    pass

# Mount Routers under API v1 prefix
app.include_router(predictions.router, prefix=settings.API_V1_STR)
app.include_router(predictions.query_router, prefix=settings.API_V1_STR)
app.include_router(jobs.router, prefix=settings.API_V1_STR)
app.include_router(models.router, prefix=settings.API_V1_STR)

@app.get("/health", response_model=HealthResponse)
def health_check() -> Dict[str, Any]:
    """Health check endpoint used by Docker healthchecks and monitoring."""
    db = mongo_manager.get_database()
    return {
        "status": "UP",
        "service": "ai-engine",
        "version": settings.VERSION,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "mongoConnected": db is not None
    }

@app.get("/metrics")
def get_metrics() -> str:
    """Prometheus metrics endpoint format."""
    return (
        "# HELP ai_engine_status Service status indicator (1 = UP)\n"
        "# TYPE ai_engine_status gauge\n"
        "ai_engine_status 1\n"
        "# HELP ai_predictions_total Total AI predictions calculated\n"
        "# TYPE ai_predictions_total counter\n"
        f"ai_predictions_total {len(mongo_manager._mock_predictions)}\n"
    )

@app.get("/")
def root():
    return {
        "service": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "docs": "/docs",
        "endpoints": {
            "health": "/health",
            "predictions": f"{settings.API_V1_STR}/predict",
            "models": f"{settings.API_V1_STR}/models",
            "jobs": f"{settings.API_V1_STR}/jobs"
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
