#!/bin/bash

set -e


echo ">>>>>>>>>> STARTING PARTYCONTACT SERVICE"
curl -X POST http://localhost:9100/server/start/partycontact-rest
echo ">>> [OK] START PARTYCONTACT-REST SERVER"

curl -X POST http://localhost:9201/consumer/startDefaultConsumer
echo "         [OK] START PARTYCONTACT DEFAULT CONSUMER"

echo " [OK] STARTUP-PARTYCONTACT-BASE PROCESS FINISHED."


