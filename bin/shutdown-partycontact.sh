#!/bin/bash

set -e


echo ">>>>>>>>>>> STOPPING PARTYCONTACT-REST SERVICES"

curl -X POST http://localhost:9201/consumer/shutdown
echo "         [OK] STOP PARTYCNTACT CONSUMER"


echo ">>>>>>>>>>> STOPPING PARTYCONTACT-REST"
curl -X POST http://localhost:9100/server/stop/partycontact-rest
echo ">>> [OK] STOP PARTYCONTAT-REST SERVER"


echo " [OK] SHUTDOWN-PARTYCONTACT PROCESS FINISHED."

