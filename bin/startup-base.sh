#!/bin/bash

set -e


echo ">>>>>>>>>> STARTING LOGMINER CONNECTOR SERVICE"

curl -X POST http://localhost:9100/server/start/logminer-rest
echo ">>> [OK] START LOGMINER-REST SERVER"

curl -X POST http://localhost:9102/logminer/startConnector/reset
echo "         [OK] START LOGMINER CONNECT W/ RESET"


echo ">>>>>>>>>> STARTING HEALTH SERVICE"

curl -X POST http://localhost:9100/server/start/health-rest
echo ">>> [OK] START HEALTH-REST SERVER"

curl -X POST http://localhost:9103/health/startServerCheck
echo "         [OK] START SERVER CHECK"

curl -X POST http://localhost:9103/health/applyLogminerSync
echo "         [OK] APPLY LOGMINER SYNC"

curl -X POST http://localhost:9103/health/startHeartbeat
echo "         [OK] START HEARTBEAT"

curl -X POST http://localhost:9103/health/startHealthConsumer
echo "         [OK] START HEALTH CONSUMER"

curl -X POST http://localhost:9103/health/startConsumerCheck
echo "         [OK] START CONSUMER CHECK"


echo " [OK] STARTUP-BASE PROCESS FINISHED."


