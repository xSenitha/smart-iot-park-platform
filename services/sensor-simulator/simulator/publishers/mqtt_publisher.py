import json
import logging
from typing import Dict, Any, Optional

logger = logging.getLogger("simulator.mqtt")

class MqttPublisher:
    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port
        self.client: Optional[Any] = None
        self.connected = False

    def connect(self) -> bool:
        try:
            import paho.mqtt.client as mqtt
            # Support both Paho v1 and v2 API
            try:
                self.client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="smartpark-simulator-daemon")
            except (AttributeError, TypeError):
                self.client = mqtt.Client(client_id="smartpark-simulator-daemon")

            def on_connect(client, userdata, flags, rc, properties=None):
                if rc == 0:
                    self.connected = True
                    logger.info("Successfully connected to Mosquitto MQTT Broker at %s:%d", self.host, self.port)
                else:
                    logger.warning("MQTT connection failed with code: %s", rc)

            def on_disconnect(client, userdata, rc, properties=None):
                self.connected = False
                logger.info("Disconnected from MQTT Broker (rc=%s)", rc)

            self.client.on_connect = on_connect
            self.client.on_disconnect = on_disconnect
            
            logger.info("Connecting to Mosquitto MQTT Broker at %s:%d...", self.host, self.port)
            self.client.connect(self.host, self.port, keepalive=60)
            self.client.loop_start()
            return True
        except ImportError:
            logger.warning("paho-mqtt library is not installed in current environment. Running in mock MQTT mode.")
            return False
        except Exception as e:
            logger.warning("Could not establish initial connection to MQTT Broker (%s:%d): %s", self.host, self.port, e)
            return False

    def publish_telemetry(self, park_id: int, zone_id: int, device_id: str, payload: Dict[str, Any]) -> bool:
        topic = f"parks/{park_id}/zones/{zone_id}/devices/{device_id}/telemetry"
        data_str = json.dumps(payload)

        if self.client and self.connected:
            try:
                result = self.client.publish(topic, data_str, qos=1)
                return result.rc == 0
            except Exception as e:
                logger.error("Error publishing to MQTT topic %s: %s", topic, e)
                return False
        else:
            logger.debug("[MOCK MQTT PUBLISH] Topic: %s | Payload: %s", topic, data_str)
            return True

    def disconnect(self):
        if self.client:
            try:
                self.client.loop_stop()
                self.client.disconnect()
            except Exception:
                pass
            self.connected = False
