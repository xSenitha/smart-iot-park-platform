import unittest
from datetime import datetime, timezone, timedelta
from app.ml.equipment_anomaly import EquipmentAnomalyDetector

class TestEquipmentAnomalyDetector(unittest.TestCase):
    def setUp(self):
        self.detector = EquipmentAnomalyDetector(threshold=0.70)

    def test_insufficient_data(self):
        res = self.detector.detect("DEV-BIN-0001", "BIN", [])
        self.assertFalse(res["isAnomaly"])
        self.assertEqual(res["probableRootCause"], "INSUFFICIENT_DATA")

    def test_normal_device_operation(self):
        t1 = datetime(2026, 10, 18, 10, 0, 0, tzinfo=timezone.utc)
        t2 = t1 + timedelta(hours=1)
        readings = [
            {"timestamp": t1.isoformat(), "metrics": {"batteryLevel": 95.0, "tiltAngle": 1.5}},
            {"timestamp": t2.isoformat(), "metrics": {"batteryLevel": 94.9, "tiltAngle": 1.6}}
        ]
        res = self.detector.detect("DEV-BIN-0001", "BIN", readings)
        self.assertFalse(res["isAnomaly"])
        self.assertLess(res["anomalyScore"], 0.70)

    def test_rapid_battery_drain_anomaly(self):
        t1 = datetime(2026, 10, 18, 10, 0, 0, tzinfo=timezone.utc)
        t2 = t1 + timedelta(hours=1)
        # Drop 30% battery in 1 hour with low remaining
        readings = [
            {"timestamp": t1.isoformat(), "metrics": {"batteryLevel": 40.0}},
            {"timestamp": t2.isoformat(), "metrics": {"batteryLevel": 10.0}}
        ]
        res = self.detector.detect("DEV-BIN-0001", "BIN", readings)
        self.assertTrue(res["isAnomaly"])
        self.assertIn("CRITICAL_BATTERY_LEVEL", res["probableRootCause"])
        self.assertIn("RAPID_BATTERY_DRAIN_FAULT", res["probableRootCause"])

    def test_bin_tilt_tampering(self):
        t1 = datetime(2026, 10, 18, 10, 0, 0, tzinfo=timezone.utc)
        t2 = t1 + timedelta(minutes=15)
        readings = [
            {"timestamp": t1.isoformat(), "metrics": {"batteryLevel": 80.0, "tiltAngle": 2.0}},
            {"timestamp": t2.isoformat(), "metrics": {"batteryLevel": 80.0, "tiltAngle": 45.0}}
        ]
        res = self.detector.detect("DEV-BIN-0001", "BIN", readings)
        self.assertIn("BIN_TILT_TAMPERING", res["probableRootCause"])
        self.assertGreater(res["anomalyScore"], 0.60)

    def test_light_burnout_at_night(self):
        # 22:00 at night: lamp is on, but 0 watts power consumption
        t1 = datetime(2026, 10, 18, 22, 0, 0, tzinfo=timezone.utc)
        t2 = t1 + timedelta(minutes=10)
        readings = [
            {"timestamp": t1.isoformat(), "metrics": {"batteryLevel": 85.0, "isLampOn": True, "powerConsumptionWatts": 0.5}},
            {"timestamp": t2.isoformat(), "metrics": {"batteryLevel": 85.0, "isLampOn": True, "powerConsumptionWatts": 0.5}}
        ]
        res = self.detector.detect("DEV-LIGHT-0001", "LIGHT", readings)
        self.assertTrue(res["isAnomaly"])
        self.assertIn("LED_DRIVER_OR_BULB_BURNOUT", res["probableRootCause"])

if __name__ == "__main__":
    unittest.main()
