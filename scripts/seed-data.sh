#!/usr/bin/env bash
set -euo pipefail

echo "========================================================"
echo "Smart IoT Park Platform: Seeding Initial Demonstration Data"
echo "========================================================"

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"

echo "Checking Gateway availability at ${GATEWAY_URL}..."
until curl -s "${GATEWAY_URL}/actuator/health" > /dev/null 2>&1 || curl -s "${GATEWAY_URL}/api/v1/auth/login" > /dev/null 2>&1; do
    echo "Waiting for API Gateway to be ready..."
    sleep 3
done

echo "Platform is online. Seeding completed via Flyway baseline and Mongo Init."
echo "Demo Accounts Ready:"
echo " - Admin:       admin / Admin123!"
echo " - CityPlanner: planner / Planner123!"
echo " - Maintenance: maintenance / Tech123!"
