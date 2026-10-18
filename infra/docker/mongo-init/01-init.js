// MongoDB Initialization Script for Smart IoT Park Platform

db = db.getSiblingDB('smartpark_telemetry');

// Create application user
db.createUser({
  user: 'mongo_user',
  pwd: 'mongo_dev_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'smartpark_telemetry'
    }
  ]
});

// Create collections
db.createCollection('sensor_readings');
db.createCollection('predictions');
db.createCollection('model_metadata');

// Indices on sensor_readings
// 1. Time-series indexing: deviceId + timestamp descending
db.sensor_readings.createIndex({ "deviceId": 1, "timestamp": -1 });

// 2. Zone-level querying
db.sensor_readings.createIndex({ "zoneId": 1, "timestamp": -1 });

// 3. TTL index: automatically expire raw telemetry after 90 days (7,776,000 seconds)
db.sensor_readings.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 7776000 });

// Indices on predictions
db.predictions.createIndex({ "deviceId": 1, "generatedAt": -1 });
db.predictions.createIndex({ "zoneId": 1, "generatedAt": -1 });
db.predictions.createIndex({ "predictionType": 1, "generatedAt": -1 });

// Indices on model_metadata
db.model_metadata.createIndex({ "modelName": 1, "modelVersion": 1 }, { unique: true });

print("MongoDB smartpark_telemetry initialization complete with indexes & TTL.");
