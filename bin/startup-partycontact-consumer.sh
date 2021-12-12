#!/bin/bash

set -e

echo ">>>>>>>>>> STARTING PARTYCONTACT CONSUMER SERVICE"

curl -X POST http://localhost:9201/consumer/stopConsumer
echo "         [OK] STOP PARTYCONTACT CONSUMER"

curl -X POST http://localhost:9201/consumer/startPartyContactConsumer
echo "         [OK] START PARTYCONTACT CONSUMER"

echo " [OK] STARTUP-PARTYCONTACT-CONSUMER PROCESS FINISHED."


