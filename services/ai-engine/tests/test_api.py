import unittest
from app.main import health_check, root, get_metrics
from app.api.routers.models import list_models, get_model_by_name
from app.api.routers.jobs import trigger_batch_prediction_job, get_job_status
from app.api.routers.predictions import predict_bin_overflow, detect_hardware_anomaly, score_irrigation_need
from app.api.schemas import (
    OverflowPredictionRequest,
    AnomalyDetectionRequest,
    IrrigationScoringRequest
)
from app.core.compat import HTTPException

class TestApiAndJobPipeline(unittest.TestCase):
    def test_health_check(self):
        res = health_check()
        self.assertEqual(res["status"], "UP")
        self.assertEqual(res["service"], "ai-engine")
        self.assertIn("timestamp", res)

    def test_root_and_metrics(self):
        r = root()
        self.assertEqual(r["version"], "1.0.0")
        metrics = get_metrics()
        self.assertIn("ai_engine_status 1", metrics)

    def test_model_registry(self):
        models = list_models()
        self.assertGreaterEqual(len(models), 3)
        names = [m["modelName"] for m in models]
        self.assertIn("bin_overflow_forecaster", names)
        self.assertIn("equipment_anomaly_detector", names)
        self.assertIn("irrigation_need_scorer", names)

        bin_model = get_model_by_name("bin_overflow_forecaster")
        self.assertEqual(bin_model["modelVersion"], "v1.2.0")

        with self.assertRaises(HTTPException):
            get_model_by_name("non_existent_model")

    def test_prediction_endpoints(self):
        # 1. Overflow
        req1 = OverflowPredictionRequest(
            deviceId="DEV-BIN-0001",
            readings=[
                {"timestamp": "2026-10-18T06:00:00Z", "metrics": {"fillPercentage": 50.0}},
                {"timestamp": "2026-10-18T07:00:00Z", "metrics": {"fillPercentage": 60.0}}
            ]
        )
        res1 = predict_bin_overflow(req1)
        self.assertEqual(res1["currentFill"], 60.0)
        self.assertIn(res1["riskLevel"], ["LOW", "MEDIUM", "HIGH", "CRITICAL"])

        # 2. Anomaly
        req2 = AnomalyDetectionRequest(
            deviceId="DEV-BIN-0001",
            deviceType="BIN",
            readings=[
                {"timestamp": "2026-10-18T06:00:00Z", "metrics": {"batteryLevel": 90.0}},
                {"timestamp": "2026-10-18T07:00:00Z", "metrics": {"batteryLevel": 89.9}}
            ]
        )
        res2 = detect_hardware_anomaly(req2)
        self.assertEqual(res2["deviceId"], "DEV-BIN-0001")
        self.assertFalse(res2["isAnomaly"])

        # 3. Irrigation
        req3 = IrrigationScoringRequest(
            zoneId=1,
            targetMoisture=40.0,
            readings=[{"metrics": {"soilMoisture": 38.0}}]
        )
        res3 = score_irrigation_need(req3)
        self.assertEqual(res3["zoneId"], 1)
        self.assertEqual(res3["currentMoisture"], 38.0)

    def test_batch_prediction_job_execution(self):
        job_result = trigger_batch_prediction_job()
        self.assertEqual(job_result["status"], "COMPLETED")
        self.assertGreaterEqual(job_result["devicesEvaluated"], 9)
        self.assertIn("timestamp", job_result)

        status = get_job_status()
        self.assertEqual(status["status"], "COMPLETED")
        self.assertIsNotNone(status["lastRun"])

if __name__ == "__main__":
    unittest.main()
