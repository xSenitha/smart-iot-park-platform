import os
from dataclasses import dataclass

@dataclass
class Settings:
    PROJECT_NAME: str = "Smart IoT Park - AI Engine"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"

    # MongoDB persistence
    MONGO_URI: str = os.getenv("MONGO_URI", "mongodb://localhost:27017/smartpark_telemetry")
    MONGO_DB_NAME: str = os.getenv("MONGO_DB_NAME", "smartpark_telemetry")

    # Core API Integration
    CORE_API_URL: str = os.getenv("CORE_API_URL", "http://localhost:8081")

    # Prediction Job & Thresholds
    AI_PREDICTION_CRON_INTERVAL_MINUTES: int = int(os.getenv("AI_PREDICTION_CRON_INTERVAL_MINUTES", "5"))
    BIN_OVERFLOW_WARNING_HOURS: float = float(os.getenv("BIN_OVERFLOW_WARNING_HOURS", "3.0"))
    EQUIPMENT_ANOMALY_THRESHOLD: float = float(os.getenv("EQUIPMENT_ANOMALY_THRESHOLD", "0.75"))
    SOIL_MOISTURE_CRITICAL_DEFICIT: float = float(os.getenv("SOIL_MOISTURE_CRITICAL_DEFICIT", "20.0"))

settings = Settings()
