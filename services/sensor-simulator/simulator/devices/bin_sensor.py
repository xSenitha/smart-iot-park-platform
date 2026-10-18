import random
import math
from datetime import datetime, timezone
from typing import Dict, Any
from .base import IoTDeviceSimulator

class BinSimulator(IoTDeviceSimulator):
    def __init__(
        self,
        device_id: int,
        device_uid: str,
        name: str,
        zone_id: int,
        park_id: int,
        initial_fill: float = 30.0,
        initial_battery: float = 95.0,
        fill_rate_per_step: float = 0.4
    ):
        super().__init__(
            device_id=device_id,
            device_uid=device_uid,
            name=name,
            device_type="BIN",
            zone_id=zone_id,
            park_id=park_id,
            initial_battery=initial_battery,
            model="EcoCompactor-X1",
            firmware_version="v2.1.0"
        )
        self.fill_percentage = initial_fill
        self.fill_rate = fill_rate_per_step
        self.lid_open_count = random.randint(10, 50)
        self.tilt_angle = round(random.uniform(0.5, 2.0), 1)

    def step(self, anomaly_rate: float = 0.05) -> Dict[str, Any]:
        hour = datetime.now(timezone.utc).hour
        # Crowd multiplier: park bins fill faster during midday (11:00 - 15:00) and evening (17:00 - 20:00)
        crowd_factor = 2.0 if (11 <= hour <= 15 or 17 <= hour <= 20) else 0.8
        
        # Inject occasional anomaly
        if random.random() < anomaly_rate and not self.anomaly_active:
            self.anomaly_active = True
            anomaly_choice = random.choice(["SURGE_FILL", "TILT_FAULT", "BATTERY_DRAIN"])
            self.anomaly_type = anomaly_choice
        elif self.anomaly_active and random.random() < 0.25:
            # Clear anomaly
            self.anomaly_active = False
            self.anomaly_type = None

        if self.anomaly_type == "SURGE_FILL":
            self.fill_percentage += random.uniform(8.0, 15.0)
        elif self.anomaly_type == "TILT_FAULT":
            self.tilt_angle = round(random.uniform(35.0, 55.0), 1)
        elif self.anomaly_type == "BATTERY_DRAIN":
            self.drain_battery(rate=0.8)
        else:
            # Normal fill progression
            delta = self.fill_rate * crowd_factor * random.uniform(0.6, 1.4)
            self.fill_percentage += delta
            self.tilt_angle = round(random.uniform(0.2, 2.5), 1)
            self.drain_battery(rate=0.01)

        # Increment lid interactions
        if random.random() < 0.6:
            self.lid_open_count += random.randint(1, 3)

        # If bin overflows beyond 96%, simulate collection crew emptying it after a brief peak
        if self.fill_percentage >= 96.0:
            if random.random() < 0.35:
                # Serviced and emptied
                self.fill_percentage = round(random.uniform(2.0, 8.0), 1)
                self.lid_open_count = 0

        self.fill_percentage = min(100.0, max(0.0, round(self.fill_percentage, 1)))
        
        # Ambient temperature diurnal cycle (sine wave: 18 - 28 C)
        ambient_temp = round(23.0 + 5.0 * math.sin((hour - 8) * math.pi / 12), 1)

        metrics = {
            "fillPercentage": self.fill_percentage,
            "batteryLevel": self.battery_level,
            "temperatureCelsius": ambient_temp,
            "lidOpenCount": self.lid_open_count,
            "tiltAngle": self.tilt_angle
        }

        return self.build_payload(metrics)
