import unittest
from simulator.devices.bin_sensor import BinSimulator
from simulator.devices.soil_sensor import SoilSimulator
from simulator.devices.light_sensor import LightSimulator
from simulator.fleet import build_park_fleet
from simulator.publishers.mqtt_publisher import MqttPublisher
from simulator.publishers.rest_publisher import RestPublisher

class TestSensorSimulator(unittest.TestCase):

    def test_bin_simulator_step_and_payload(self):
        bin_sim = BinSimulator(1, "DEV-BIN-0001", "Plaza Bin", zone_id=1, park_id=1, initial_fill=50.0)
        payload = bin_sim.step(anomaly_rate=0.0)

        self.assertEqual(payload["deviceId"], "DEV-BIN-0001")
        self.assertEqual(payload["deviceType"], "BIN")
        self.assertEqual(payload["zoneId"], 1)
        self.assertEqual(payload["parkId"], 1)
        self.assertIn("timestamp", payload)

        metrics = payload["metrics"]
        self.assertIn("fillPercentage", metrics)
        self.assertIn("batteryLevel", metrics)
        self.assertIn("temperatureCelsius", metrics)
        self.assertIn("lidOpenCount", metrics)
        self.assertIn("tiltAngle", metrics)

        self.assertGreaterEqual(metrics["fillPercentage"], 0.0)
        self.assertLessEqual(metrics["fillPercentage"], 100.0)
        self.assertGreaterEqual(metrics["batteryLevel"], 0.0)
        self.assertLessEqual(metrics["batteryLevel"], 100.0)

    def test_soil_simulator_step_and_payload(self):
        soil_sim = SoilSimulator(7, "DEV-SOIL-0001", "Lawn Soil", zone_id=1, park_id=1, initial_moisture=45.0)
        payload = soil_sim.step(anomaly_rate=0.0)

        self.assertEqual(payload["deviceId"], "DEV-SOIL-0001")
        self.assertEqual(payload["deviceType"], "SOIL")
        metrics = payload["metrics"]
        self.assertIn("soilMoisture", metrics)
        self.assertIn("soilTemperature", metrics)
        self.assertIn("electricalConductivity", metrics)

        self.assertGreaterEqual(metrics["soilMoisture"], 0.0)
        self.assertLessEqual(metrics["soilMoisture"], 100.0)

    def test_light_simulator_step_and_payload(self):
        light_sim = LightSimulator(12, "DEV-LIGHT-0001", "Plaza Light", zone_id=1, park_id=1)
        payload = light_sim.step(anomaly_rate=0.0)

        self.assertEqual(payload["deviceId"], "DEV-LIGHT-0001")
        self.assertEqual(payload["deviceType"], "LIGHT")
        metrics = payload["metrics"]
        self.assertIn("lightLux", metrics)
        self.assertIn("powerConsumptionWatts", metrics)
        self.assertIn("isLampOn", metrics)

        self.assertGreaterEqual(metrics["lightLux"], 0.0)
        self.assertGreaterEqual(metrics["powerConsumptionWatts"], 0.0)

    def test_fleet_generation(self):
        fleet = build_park_fleet(park_id=1)
        self.assertEqual(len(fleet), 15)

        bin_count = sum(1 for d in fleet if d.device_type == "BIN")
        soil_count = sum(1 for d in fleet if d.device_type == "SOIL")
        light_count = sum(1 for d in fleet if d.device_type == "LIGHT")

        self.assertEqual(bin_count, 6)
        self.assertEqual(soil_count, 5)
        self.assertEqual(light_count, 4)

        zone_ids = {d.zone_id for d in fleet}
        self.assertEqual(zone_ids, {1, 2, 3})

    def test_mqtt_publisher_mock_mode(self):
        pub = MqttPublisher("localhost", 1883)
        # In mock mode, publish_telemetry returns True without throwing exception
        success = pub.publish_telemetry(1, 1, "DEV-BIN-0001", {"test": 123})
        self.assertTrue(success)

if __name__ == "__main__":
    unittest.main()
