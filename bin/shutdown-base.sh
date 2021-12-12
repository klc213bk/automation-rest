#!/bin/bash

set -e




echo ">>>>>>>>>>> STOPPING HEALTH-RES SERVICEST"

curl -X POST http://localhost:9103/health/stopConsumerCheck
echo "         [OK] STOP CONSUMER CHECK"

curl -X POST http://localhost:9103/health/stoptHealthConsumer
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



echo ">>>>>>>>>>> STOPPING LOGMINER-REST SERVICES"

curl -X POST http://localhost:9102/logminer/stopConnector
echo "         [OK] STOP LOGMINER CONNECT"

echo ">>>>>>>>>>> STOPPING LOGMINER-REST"
curl -X POST http://localhost:9100/server/stop/logminer-rest
echo ">>> [OK] STOP LOGMINER-REST SERVER"



echo ">>>>>>>>>>> STOPPING KAFKA-REST SERVICES"

curl -X POST http://localhost:9101/kafka/stopKafka
echo "         [OK] STOP KAFKA SERVER"

curl -X POST http://localhost:9101/kafka/stopZookeeper
echo "         [OK] STOP ZOOKEEPER SERVER"


echo ">>>>>>>>>>> STOPPING KAFKA-REST"
curl -X POST http://localhost:9100/server/stop/kafka-rest
echo ">>> [OK] STOP KAFKA-REST SERVER"


echo " [OK] SHUTDOWN-BASE PROCESS FINISHED."



