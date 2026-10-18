import random
import math
from datetime import datetime, timezone
from typing import Dict, Any
from .base import IoTDeviceSimulator

class LightSimulator(IoTDeviceSimulator):
    def __init__(
        self,
        device_id: int,
        device_uid: str,
        name: str,
        zone_id: int,
        park_id: int,
        initial_battery: float = 99.0
    ):
        super().__init__(
            device_id=device_id,
            device_uid=device_uid,
            name=name,
            device_type="LIGHT",
            zone_id=zone_id,
            park_id=park_id,
            initial_battery=initial_battery,
            model="LumiGrid-SmartLED",
            firmware_version="v3.0.1"
        )
        self.is_lamp_on = False
        self.brightness = 0

    def step(self, anomaly_rate: float = 0.05) -> Dict[str, Any]:
        hour = datetime.now(timezone.utc).hour
        is_night = (hour < 6 or hour >= 18)

        # Anomaly simulation
        if random.random() < anomaly_rate and not self.anomaly_active:
            self.anomaly_active = True
            self.anomaly_type = random.choice(["BULB_OUTAGE", "DAY_BURN_FAULT", "VOLTAGE_SPIKE"])
        elif self.anomaly_active and random.random() < 0.25:
            self.anomaly_active = False
            self.anomaly_type = None

        if self.anomaly_type == "BULB_OUTAGE":
            self.is_lamp_on = False
            self.brightness = 0
            power_watts = 0.0
            ambient_lux = random.uniform(1.0, 8.0) if is_night else random.uniform(8000.0, 35000.0)
        elif self.anomaly_type == "DAY_BURN_FAULT":
            # Burning during bright day
            self.is_lamp_on = True
            self.brightness = 100
            power_watts = 65.0
            ambient_lux = random.uniform(15000.0, 45000.0)
            self.drain_battery(rate=0.08)
        else:
            # Normal smart lighting schedule
            if is_night:
                self.is_lamp_on = True
                self.brightness = 85
                power_watts = round(random.uniform(48.0, 54.0), 1)
                ambient_lux = round(random.uniform(5.0, 25.0), 1) # Illuminated path lux
                self.drain_battery(rate=0.02)
            else:
                self.is_lamp_on = False
                self.brightness = 0
                power_watts = 0.8 # Standby sensor draw
                # Daytime sun sine wave
                sun_intensity = max(0.0, math.sin((hour - 6) * math.pi / 12))
                ambient_lux = round(1000.0 + 40000.0 * sun_intensity + random.uniform(-500.0, 500.0), 1)
                # Solar recharge during day
                self.battery_level = min(100.0, round(self.battery_level + 0.03, 2))

        metrics = {
            "lightLux": ambient_lux,
            "powerConsumptionWatts": power_watts,
            "isLampOn": self.is_lamp_on,
            "brightnessPercentage": self.brightness,
            "batteryLevel": self.battery_level
        }

        return self.build_payload(metrics)
