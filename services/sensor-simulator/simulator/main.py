import sys
import time
import signal
import logging
from .config import config
from .fleet import build_park_fleet
from .publishers.mqtt_publisher import MqttPublisher
from .publishers.rest_publisher import RestPublisher

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] [%(levelname)s] [%(name)s]: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger("simulator")

def run_simulator():
    logger.info("=================================================================")
    logger.info("Starting Smart IoT Park Platform: Sensor Simulator Fleet Daemon")
    logger.info("Fleet Scope: Park %d | Interval: %.1fs | Anomaly Rate: %.2f",
                config.park_id, config.interval_seconds, config.anomaly_rate)
    logger.info("=================================================================")

    fleet = build_park_fleet(config.park_id)
    logger.info("Initialized fleet with %d virtual IoT edge devices.", len(fleet))

    mqtt_pub = MqttPublisher(config.mqtt_broker_host, config.mqtt_broker_port)
    rest_pub = RestPublisher(config.ingestion_http_url)

    if config.mqtt_enabled:
        mqtt_pub.connect()

    running = True

    def handle_signal(sig, frame):
        nonlocal running
        logger.info("Shutdown signal received (%s). Gracefully stopping simulator...", sig)
        running = False

    signal.signal(signal.SIGINT, handle_signal)
    signal.signal(signal.SIGTERM, handle_signal)

    iteration = 0
    while running:
        iteration += 1
        start_time = time.time()
        published_count = 0

        for device in fleet:
            payload = device.step(anomaly_rate=config.anomaly_rate)
            
            # 1. Publish via MQTT
            if config.mqtt_enabled:
                mqtt_pub.publish_telemetry(
                    park_id=device.park_id,
                    zone_id=device.zone_id,
                    device_id=device.device_uid,
                    payload=payload
                )
                published_count += 1

            # 2. Publish via REST (if enabled)
            if config.rest_enabled:
                rest_pub.publish_telemetry(payload)

        elapsed = time.time() - start_time
        logger.info("Cycle #%d: Dispatched %d readings across %d devices in %.2fs",
                    iteration, published_count, len(fleet), elapsed)

        sleep_time = max(0.1, config.interval_seconds - elapsed)
        time.sleep(sleep_time)

    mqtt_pub.disconnect()
    logger.info("Sensor simulator stopped cleanly.")

if __name__ == "__main__":
    run_simulator()
