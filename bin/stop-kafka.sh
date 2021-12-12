#!/bin/bash

set -e


curl -X POST http://localhost:9101/kafka/stopKafka
echo "         [OK] STOP KAFKA SERVER"


curl -X POST http://localhost:9101/kafka/stopookeeper
echo "         [OK] STOP ZOOKEEPER SERVER"

curl -X POST http://localhost:9100/server/stop/kafka-rest
echo ">>> [OK] STOP KAFKA-REST SERVER"

echo " [OK] STOP-KAFKA PROCESS FINISHED."


