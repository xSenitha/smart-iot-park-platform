import json
import logging
import http.client
import urllib.parse
from typing import List, Dict, Any, Optional
from app.core.config import settings

logger = logging.getLogger("ai_engine.core_api")

class CoreApiClient:
    def __init__(self, base_url: str = settings.CORE_API_URL):
        self.base_url = base_url
        self.parsed = urllib.parse.urlparse(base_url)

    def _request(self, method: str, path: str, body: Optional[Dict[str, Any]] = None) -> Optional[Dict[str, Any]]:
        headers = {"Content-Type": "application/json"}
        data_bytes = json.dumps(body).encode("utf-8") if body is not None else None

        try:
            if self.parsed.scheme == "https":
                conn = http.client.HTTPSConnection(self.parsed.netloc, timeout=4)
            else:
                conn = http.client.HTTPConnection(self.parsed.netloc, timeout=4)

            full_path = self.parsed.path.rstrip("/") + path
            conn.request(method, full_path, body=data_bytes, headers=headers)
            res = conn.getresponse()
            raw_data = res.read()
            conn.close()

            if res.status in (200, 201):
                return json.loads(raw_data.decode("utf-8"))
            else:
                logger.debug("Core API request %s %s returned status %d: %s", method, path, res.status, raw_data)
                return None
        except Exception as e:
            logger.debug("Core API communication unavailable (%s): %s", path, e)
            return None

    def get_devices(self) -> List[Dict[str, Any]]:
        res = self._request("GET", "/api/v1/devices")
        if res and res.get("success") and "data" in res:
            return res["data"]
        return []

    def create_alert(self, device_id: int, alert_type: str, severity: str, title: str, message: str) -> Optional[Dict[str, Any]]:
        body = {
            "deviceId": device_id,
            "alertType": alert_type,
            "severity": severity,
            "title": title,
            "message": message
        }
        return self._request("POST", "/api/v1/alerts", body)

    def create_maintenance_ticket(self, park_id: int, zone_id: int, device_id: Optional[int], title: str, description: str, priority: str, source: str = "AI_PREDICTION") -> Optional[Dict[str, Any]]:
        body = {
            "parkId": park_id,
            "zoneId": zone_id,
            "deviceId": device_id,
            "title": title,
            "description": description,
            "priority": priority,
            "source": source
        }
        return self._request("POST", "/api/v1/tickets", body)

core_api_client = CoreApiClient()
