#!/bin/bash

set -e

echo ">>>>>>>>>>> STOPPING LOGMINER-REST SERVICES"

curl -X POST http://localhost:9102/logminer/stopConnector
echo "         [OK] STOP LOGMINER CONNECT"

echo ">>>>>>>>>>> STOPPING LOGMINER-REST"
curl -X POST http://localhost:9100/server/stop/logminer-rest
echo ">>> [OK] STOP LOGMINER-REST SERVER"




echo " [OK] SHUTDOWN-LOGMINER PROCESS FINISHED."



