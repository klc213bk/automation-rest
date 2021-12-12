#!/bin/bash

set -e


echo ">>>>>>>>>> CLEAN UP"

curl -X POST http://localhost:9100/server/start/partycontact-rest
echo ">>> [OK] START PARTYCONTACT-REST SERVER"

curl -X POST http://localhost:9201/partycontact/cleanup
echo "         [OK] CLEAN UP PARTYCONTACT ENVIRONMENT"


echo ">>>>>>>>>> INITIALIZE"

curl -X POST http://localhost:9201/partycontact/initialize
echo "         [OK] PARTYCONTACT INITIALIZATON"

echo ">>>>>>>>>>> STOPPING PARTYCONTACT-REST SERVER"
curl -X POST http://localhost:9100/server/stop/partycontact-rest
echo ">>> [OK] STOP PARTYCONTACT-REST SERVER"

echo " [OK] CLEAN AND INITIALIZATION-PARTYCONTACT PROCESS FINISHED."



