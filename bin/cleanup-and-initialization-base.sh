#!/bin/bash

set -e

echo ">>>>>>>>>> CLEAN UP"

curl -X POST http://localhost:9100/server/start/health-rest
echo ">>> [OK] START HEALTH-REST SERVER"

curl -X POST http://localhost:9103/health/cleanup
echo "         [OK] CLEAN UP HEALTH ENVIRONMENT"

echo ">>>>>>>>>> INITIALIZE"

curl -X POST http://localhost:9103/health/initialize
echo "         [OK] HEALTH INITIALIZATON"



echo ">>>>>>>>>>> STOPPING HEALTH-REST SERVER"
curl -X POST http://localhost:9100/server/stop/health-rest
echo ">>> [OK] STOP HEALTH-REST SERVER"

echo " [OK] CLEAN AND INITIALIZATION-BASE PROCESS FINISHED."




