#!/bin/bash

set -e


echo ">>>>>>>>>> STARTING LOGMINER CONNECTOR SERVICE"

curl -X POST http://localhost:9100/server/start/logminer-rest
echo ">>> [OK] START LOGMINER-REST SERVER"

curl -X POST http://localhost:9102/logminer/startConnector/reset
echo "         [OK] START LOGMINER CONNECT W/ RESET"


echo " [OK] STARTUP-LOGMINER PROCESS FINISHED."


