import random
import math
from datetime import datetime, timezone
from typing import Dict, Any
from .base import IoTDeviceSimulator

class SoilSimulator(IoTDeviceSimulator):
    def __init__(
        self,
        device_id: int,
        device_uid: str,
        name: str,
        zone_id: int,
        park_id: int,
        initial_moisture: float = 48.0,
        initial_battery: float = 98.0
    ):
        super().__init__(
            device_id=device_id,
            device_uid=device_uid,
            name=name,
            device_type="SOIL",
            zone_id=zone_id,
            park_id=park_id,
            initial_battery=initial_battery,
            model="TerraProbe-S4",
            firmware_version="v1.4.2"
        )
        self.soil_moisture = initial_moisture
        self.electrical_conductivity = round(random.uniform(280.0, 420.0), 1)

    def step(self, anomaly_rate: float = 0.05) -> Dict[str, Any]:
        hour = datetime.now(timezone.utc).hour

        # Evaporation is higher during midday sun (12:00 - 16:00)
        evaporation_rate = 0.35 if (12 <= hour <= 16) else 0.12

        # Anomaly simulation
        if random.random() < anomaly_rate and not self.anomaly_active:
            self.anomaly_active = True
            self.anomaly_type = random.choice(["DROUGHT_DEFICIT", "SENSOR_DISCONNECT"])
        elif self.anomaly_active and random.random() < 0.2:
            self.anomaly_active = False
            self.anomaly_type = None

        if self.anomaly_type == "DROUGHT_DEFICIT":
            self.soil_moisture = max(8.0, self.soil_moisture - 1.2)
        elif self.anomaly_type == "SENSOR_DISCONNECT":
            # Erratic reading
            self.soil_moisture = 0.0
        else:
            # Normal dry down
            self.soil_moisture -= evaporation_rate * random.uniform(0.8, 1.2)
            self.drain_battery(rate=0.005)

        # Automated irrigation or morning dew cycle
        if self.soil_moisture <= 20.0:
            # Automatic sprinkler kicks in
            self.soil_moisture = round(random.uniform(62.0, 72.0), 1)

        self.soil_moisture = min(100.0, max(0.0, round(self.soil_moisture, 1)))

        # Soil temperature has less amplitude than ambient air (lagged sine wave: 17 - 23 C)
        soil_temp = round(20.0 + 3.0 * math.sin((hour - 10) * math.pi / 12), 1)

        metrics = {
            "soilMoisture": self.soil_moisture,
            "soilTemperature": soil_temp,
            "electricalConductivity": self.electrical_conductivity,
            "batteryLevel": self.battery_level
        }

        return self.build_payload(metrics)
