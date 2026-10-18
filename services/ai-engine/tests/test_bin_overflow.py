import unittest
from datetime import datetime, timezone, timedelta
from app.ml.bin_overflow import BinOverflowPredictor

class TestBinOverflowPredictor(unittest.TestCase):
    def setUp(self):
        self.predictor = BinOverflowPredictor(target_capacity=90.0)

    def test_empty_readings(self):
        res = self.predictor.predict([])
        self.assertEqual(res["currentFill"], 0.0)
        self.assertEqual(res["riskLevel"], "LOW")
        self.assertEqual(res["hoursUntilFull"], 48.0)

    def test_single_reading(self):
        now = datetime.now(timezone.utc).isoformat()
        res = self.predictor.predict([{"timestamp": now, "metrics": {"fillPercentage": 85.0}}])
        self.assertEqual(res["currentFill"], 85.0)
        self.assertEqual(res["riskLevel"], "HIGH")

    def test_linear_fill_prediction(self):
        base_time = datetime(2026, 10, 18, 6, 0, 0, tzinfo=timezone.utc)
        # 10% per hour fill rate over 4 hours: 30%, 40%, 50%, 60%, 70%
        readings = []
        for i in range(5):
            t = base_time + timedelta(hours=i)
            readings.append({
                "timestamp": t.isoformat(),
                "metrics": {"fillPercentage": 30.0 + i * 10.0}
            })

        res = self.predictor.predict(readings)
        self.assertEqual(res["currentFill"], 70.0)
        self.assertAlmostEqual(res["fillRatePerHour"], 10.0, places=1)
        # Target capacity is 90%, current is 70%, remaining is 20%. At 10%/h -> 2.0 hours
        self.assertAlmostEqual(res["hoursUntilFull"], 2.0, places=1)
        self.assertEqual(res["riskLevel"], "CRITICAL") # <= 2.0 hours is CRITICAL
        self.assertIsNotNone(res["projectedFullTimestamp"])

    def test_low_fill_rate(self):
        base_time = datetime(2026, 10, 18, 6, 0, 0, tzinfo=timezone.utc)
        readings = [
            {"timestamp": base_time.isoformat(), "metrics": {"fillPercentage": 20.0}},
            {"timestamp": (base_time + timedelta(hours=2)).isoformat(), "metrics": {"fillPercentage": 22.0}}
        ]
        res = self.predictor.predict(readings)
        self.assertEqual(res["currentFill"], 22.0)
        self.assertEqual(res["riskLevel"], "LOW")
        self.assertGreater(res["hoursUntilFull"], 15.0)

if __name__ == "__main__":
    unittest.main()
