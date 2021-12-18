#!/bin/bash

set -e

curl -X POST http://localhost:9100/server/start/kafka-rest
echo ">>> [OK] START KAFKA-REST SERVER"

curl -X POST http://localhost:9101/kafka/startZookeeper
echo "         [OK] START ZOOKEEPER SERVER"

curl -X POST http://localhost:9101/kafka/startKafka
echo "         [OK] START KAFKA SERVER"

sleep 15

echo " [OK] START-KAFKA PROCESS FINISHED."


