from abc import ABC, abstractmethod
from datetime import datetime, timezone
from typing import Dict, Any, Optional

class IoTDeviceSimulator(ABC):
    def __init__(
        self,
        device_id: int,
        device_uid: str,
        name: str,
        device_type: str,
        zone_id: int,
        park_id: int,
        initial_battery: float = 100.0,
        model: str = "Standard-IoT",
        firmware_version: str = "v1.0.0"
    ):
        self.device_id = device_id
        self.device_uid = device_uid
        self.name = name
        self.device_type = device_type
        self.zone_id = zone_id
        self.park_id = park_id
        self.battery_level = initial_battery
        self.model = model
        self.firmware_version = firmware_version
        
        # State & Anomaly Flags
        self.is_active = True
        self.anomaly_active = False
        self.anomaly_type: Optional[str] = None

    def drain_battery(self, rate: float = 0.005) -> float:
        """Simulate continuous battery discharge with solar recharge factor."""
        if self.battery_level > 0.0:
            self.battery_level = max(0.0, round(self.battery_level - rate, 2))
        return self.battery_level

    @abstractmethod
    def step(self, anomaly_rate: float = 0.05) -> Dict[str, Any]:
        """Advance device physics by one discrete simulation timestep."""
        pass

    def build_payload(self, metrics: Dict[str, Any], metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Construct standard telemetry JSON envelope matching Ingestion Service contract."""
        payload_meta = {
            "firmwareVersion": self.firmware_version,
            "model": self.model,
            "anomalyActive": self.anomaly_active,
        }
        if metadata:
            payload_meta.update(metadata)

        return {
            "deviceId": self.device_uid,
            "deviceType": self.device_type,
            "zoneId": self.zone_id,
            "parkId": self.park_id,
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "metrics": metrics,
            "metadata": payload_meta
        }
