import unittest
from app.ml.irrigation_need import IrrigationNeedScorer

class TestIrrigationNeedScorer(unittest.TestCase):
    def setUp(self):
        self.scorer = IrrigationNeedScorer()

    def test_no_readings_returns_fallback(self):
        res = self.scorer.score_zone(zone_id=1, target_moisture=45.0, recent_readings=[])
        self.assertEqual(res["zoneId"], 1)
        self.assertEqual(res["urgency"], "LOW")
        self.assertEqual(res["recommendedWateringMinutes"], 0)

    def test_optimal_soil_moisture(self):
        readings = [
            {"metrics": {"soilMoisture": 44.0}},
            {"metrics": {"soilMoisture": 46.0}}
        ]
        res = self.scorer.score_zone(zone_id=1, target_moisture=45.0, recent_readings=readings)
        self.assertEqual(res["currentMoisture"], 45.0)
        self.assertEqual(res["moistureDeficit"], 0.0)
        self.assertEqual(res["urgency"], "LOW")
        self.assertEqual(res["recommendedWateringMinutes"], 0)

    def test_critical_moisture_deficit(self):
        readings = [
            {"metrics": {"soilMoisture": 15.0}},
            {"metrics": {"soilMoisture": 17.0}}
        ]
        # Target 45.0, current 16.0 -> deficit 29.0
        res = self.scorer.score_zone(zone_id=2, target_moisture=45.0, recent_readings=readings)
        self.assertEqual(res["currentMoisture"], 16.0)
        self.assertEqual(res["moistureDeficit"], 29.0)
        self.assertEqual(res["urgency"], "CRITICAL")
        self.assertEqual(res["recommendedWateringMinutes"], 45)

    def test_moderate_deficit(self):
        readings = [
            {"metrics": {"soilMoisture": 37.0}},
            {"metrics": {"soilMoisture": 39.0}}
        ]
        # Target 45.0, current 38.0 -> deficit 7.0
        res = self.scorer.score_zone(zone_id=3, target_moisture=45.0, recent_readings=readings)
        self.assertEqual(res["currentMoisture"], 38.0)
        self.assertEqual(res["moistureDeficit"], 7.0)
        self.assertEqual(res["urgency"], "MODERATE")
        self.assertEqual(res["recommendedWateringMinutes"], 15)

if __name__ == "__main__":
    unittest.main()
