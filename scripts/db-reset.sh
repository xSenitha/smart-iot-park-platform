#!/usr/bin/env bash
set -euo pipefail

echo "Resetting Smart IoT Park Platform databases and volumes..."
docker compose down -v --remove-orphans
echo "Volumes removed. Restart with 'make up' or 'docker compose up -d'."
