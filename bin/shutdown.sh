#!/bin/bash

set -e






echo ">>>>>>>>>>> STOPPING KAFKA-REST"

curl -X POST http://localhost:9101/kafka/stopKafka
echo "         [OK] STOP KAFKA SERVER"

curl -X POST http://localhost:9101/kafka/stopZookeeper
echo "         [OK] STOP ZOOKEEPER SERVER"

curl -X POST http://localhost:9100/server/stop/kafka-rest
echo ">>> [OK] STOP KAFKA-REST SERVER"



curl -X POST http://localhost:9100/server/stop/health-rest
echo ">>> [OK] STOP HEALTH SERVER"


