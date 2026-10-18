import os
from dataclasses import dataclass

@dataclass
class SimulatorConfig:
    mqtt_broker_host: str = os.getenv("MQTT_BROKER_HOST", "localhost")
    mqtt_broker_port: int = int(os.getenv("MQTT_BROKER_PORT", "1883"))
    mqtt_enabled: bool = os.getenv("SIMULATOR_MQTT_ENABLED", "true").lower() == "true"
    
    ingestion_http_url: str = os.getenv("INGESTION_HTTP_URL", "http://localhost:8082/api/v1/telemetry")
    rest_enabled: bool = os.getenv("SIMULATOR_REST_ENABLED", "false").lower() == "true"
    
    interval_seconds: float = float(os.getenv("SIMULATOR_INTERVAL_SECONDS", "10.0"))
    anomaly_rate: float = float(os.getenv("SIMULATOR_ANOMALY_RATE", "0.08"))
    park_id: int = int(os.getenv("SIMULATOR_PARK_ID", "1"))

config = SimulatorConfig()
