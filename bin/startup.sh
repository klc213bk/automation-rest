#!/bin/bash

set -e

curl -X POST http://localhost:9100/server/start/kafka-rest
echo ">>> [OK] START KAFKA-REST SERVER"

curl -X POST http://localhost:9101/kafka/startZookeeper
echo "         [OK] START ZOOKEEPER SERVER"

curl -X POST http://localhost:9101/kafka/startKafka
echo "         [OK] START KAFKA SERVER"



curl -X POST http://localhost:9100/server/start/health-rest
echo ">>> [OK] START HEALTH-REST SERVER"





#curl -X POST http://localhost:9100/server/start/logminer-rest
echo ">>> [OK] START LOGMINER-REST SERVER"

#curl -X POST http://localhost:9100/server/start/health-rest
echo ">>> [OK] START HEALTH-REST SERVER"

#curl -X POST http://localhost:9100/server/start/partycontact-rest
echo ">>> [OK] START PARTYCONTAT-REST SERVER"



