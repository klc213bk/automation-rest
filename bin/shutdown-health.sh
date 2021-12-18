#!/bin/bash

set -e




echo ">>>>>>>>>>> STOPPING HEALTH-RES SERVICEST"

curl -X POST http://localhost:9103/health/stopConsumerCheck
echo "         [OK] STOP CONSUMER CHECK"

curl -X POST http://localhost:9103/health/stopHealthConsumer
echo "         [OK] STOP HEALTH CONSUMER"

curl -X POST http://localhost:9103/health/stopHeartbeat
echo "         [OK] STOP HEARTBEAT"

curl -X POST http://localhost:9103/health/dropLogminerSync
echo "         [OK] DROP LOGMINER SYNC"

curl -X POST http://localhost:9103/health/stopServerCheck
echo "         [OK] STOP SERVER CHECK"

echo ">>>>>>>>>>> STOPPING HEALTH-REST"
curl -X POST http://localhost:9100/server/stop/health-rest
echo ">>> [OK] STOP HEALTH-REST SERVER"


echo " [OK] SHUTDOWN-HEALTH-REST PROCESS FINISHED."



