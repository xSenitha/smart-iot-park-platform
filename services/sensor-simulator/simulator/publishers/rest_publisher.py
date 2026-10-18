import json
import logging
import http.client
import urllib.parse
from typing import Dict, Any

logger = logging.getLogger("simulator.rest")

class RestPublisher:
    def __init__(self, endpoint_url: str):
        self.endpoint_url = endpoint_url
        self.parsed = urllib.parse.urlparse(endpoint_url)

    def publish_telemetry(self, payload: Dict[str, Any]) -> bool:
        data_bytes = json.dumps(payload).encode("utf-8")
        headers = {
            "Content-Type": "application/json",
            "Content-Length": str(len(data_bytes))
        }

        try:
            if self.parsed.scheme == "https":
                conn = http.client.HTTPSConnection(self.parsed.netloc, timeout=5)
            else:
                conn = http.client.HTTPConnection(self.parsed.netloc, timeout=5)

            path = self.parsed.path if self.parsed.path else "/"
            conn.request("POST", path, body=data_bytes, headers=headers)
            res = conn.getresponse()
            conn.close()
            return res.status in (200, 201, 202)
        except Exception as e:
            logger.debug("REST ingestion unavailable at %s: %s", self.endpoint_url, e)
            return False
