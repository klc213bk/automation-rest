#!/bin/bash

set -e


echo ">>>>>>>>>> STARTING LOAD DATA"

curl -X POST http://localhost:9201/partycontact/applyLogminerSync
echo "         [OK] APPLY PARTYCONTACT LOGMINER SYNC"

curl -X POST http://localhost:9201/partycontact/loadAllData
echo "         [OK] LOAD PARTYCONTACT DATA"

curl -X POST http://localhost:9201/partycontact/addPrimaryKey
echo "         [OK] ADD PRIMARY KEY"

curl -X POST http://localhost:9201/partycontact/createIndexes
echo "         [OK] CREATE INDEXES"

echo " [OK] STARTUP-PARTYCONTACT-LOADDATA PROCESS FINISHED."


