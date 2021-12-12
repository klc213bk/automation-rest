#!/bin/bash

set -e


echo ">>>>>>>>>> STARTING PARTYCONTACT SERVICE"
curl -X POST http://localhost:9100/server/start/partycontact-rest
echo ">>> [OK] START PARTYCONTACT-REST SERVER"

curl -X POST http://localhost:9201/consumer/startDefaultConsumer
echo "         [OK] START PARTYCONTACT DEFAULT CONSUMER"


echo ">>>>>>>>>> STARTING LOAD DATA"

curl -X POST http://localhost:9201/partycontact/applyLogminerSync
echo "         [OK] APPLY PARTYCONTACT LOGMINER SYNC"

curl -X POST http://localhost:9201/partycontact/loadAllData
echo "         [OK] LOAD PARTYCONTACT DATA"

curl -X POST http://localhost:9201/partycontact/addPrimaryKey
echo "         [OK] ADD PRIMARY KEY"

curl -X POST http://localhost:9201/partycontact/createIndexes
echo "         [OK] CREATE INDEXES"


echo ">>>>>>>>>> STARTING PARTYCONTACT CONSUMER SERVICE"

curl -X POST http://localhost:9201/consumer/stopConsumer
echo "         [OK] STOP PARTYCONTACT CONSUMER"

curl -X POST http://localhost:9201/consumer/startPartyContactConsumer
echo "         [OK] START PARTYCONTACT CONSUMER"

echo " [OK] STARTUP-PARTYCONTACT PROCESS FINISHED."


