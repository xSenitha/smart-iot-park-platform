from datetime import datetime, timezone
from typing import List, Dict, Any, Optional
import logging
from app.core.config import settings

logger = logging.getLogger("ai_engine.db")

class MongoClientManager:
    def __init__(self):
        self._client = None
        self._db = None
        self._mock_readings: List[Dict[str, Any]] = []
        self._mock_predictions: List[Dict[str, Any]] = []
        self._mock_models: List[Dict[str, Any]] = []

    def get_database(self):
        if self._db is not None:
            return self._db

        try:
            from pymongo import MongoClient
            self._client = MongoClient(settings.MONGO_URI, serverSelectionTimeoutMS=2000)
            # Test connectivity
            self._client.admin.command("ping")
            self._db = self._client[settings.MONGO_DB_NAME]
            logger.info("Connected to MongoDB at %s", settings.MONGO_URI)
            return self._db
        except Exception as e:
            logger.debug("MongoDB not reachable (%s), using in-memory mock store for predictions.", e)
            return None

    def get_recent_readings(self, device_id: str, limit: int = 100) -> List[Dict[str, Any]]:
        db = self.get_database()
        if db is not None:
            try:
                cursor = db.sensor_readings.find(
                    {"deviceId": device_id}
                ).sort("timestamp", -1).limit(limit)
                return list(cursor)
            except Exception as e:
                logger.error("Error reading sensor_readings for %s: %s", device_id, e)

        # Fallback to in-memory filter
        return [r for r in self._mock_readings if r.get("deviceId") == device_id][:limit]

    def get_zone_readings(self, zone_id: int, device_type: str = "SOIL", limit: int = 100) -> List[Dict[str, Any]]:
        db = self.get_database()
        if db is not None:
            try:
                query: Dict[str, Any] = {"zoneId": zone_id}
                if device_type:
                    query["deviceType"] = device_type
                cursor = db.sensor_readings.find(query).sort("timestamp", -1).limit(limit)
                return list(cursor)
            except Exception as e:
                logger.error("Error reading zone telemetry for zone %d: %s", zone_id, e)

        return [r for r in self._mock_readings if r.get("zoneId") == zone_id and r.get("deviceType") == device_type][:limit]

    def save_prediction(self, prediction_doc: Dict[str, Any]) -> str:
        if "generatedAt" not in prediction_doc:
            prediction_doc["generatedAt"] = datetime.now(timezone.utc).isoformat()

        db = self.get_database()
        if db is not None:
            try:
                result = db.predictions.insert_one(prediction_doc)
                return str(result.inserted_id)
            except Exception as e:
                logger.error("Error inserting prediction to MongoDB: %s", e)

        self._mock_predictions.append(prediction_doc)
        return f"mock-pred-{len(self._mock_predictions)}"

    def save_model_metadata(self, metadata_doc: Dict[str, Any]):
        db = self.get_database()
        if db is not None:
            try:
                db.model_metadata.update_one(
                    {"modelName": metadata_doc["modelName"]},
                    {"$set": metadata_doc},
                    upsert=True
                )
                return
            except Exception as e:
                logger.error("Error updating model metadata in MongoDB: %s", e)

        self._mock_models.append(metadata_doc)

    def get_all_model_metadata(self) -> List[Dict[str, Any]]:
        db = self.get_database()
        if db is not None:
            try:
                return list(db.model_metadata.find({}, {"_id": 0}))
            except Exception as e:
                logger.error("Error fetching model metadata: %s", e)

        return self._mock_models

    def get_latest_predictions(self, limit: int = 50) -> List[Dict[str, Any]]:
        db = self.get_database()
        if db is not None:
            try:
                cursor = db.predictions.find({}, {"_id": 0}).sort("generatedAt", -1).limit(limit)
                return list(cursor)
            except Exception as e:
                logger.error("Error fetching latest predictions: %s", e)

        # Return mock predictions sorted newest first
        return sorted(self._mock_predictions, key=lambda x: x.get("generatedAt", ""), reverse=True)[:limit]

    def get_device_predictions(self, device_id: str, limit: int = 50) -> List[Dict[str, Any]]:
        db = self.get_database()
        if db is not None:
            try:
                cursor = db.predictions.find({"deviceId": device_id}, {"_id": 0}).sort("generatedAt", -1).limit(limit)
                return list(cursor)
            except Exception as e:
                logger.error("Error fetching predictions for %s: %s", device_id, e)

        return [p for p in reversed(self._mock_predictions) if p.get("deviceId") == device_id][:limit]

mongo_manager = MongoClientManager()
