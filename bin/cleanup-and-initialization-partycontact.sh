#!/bin/bash

set -e


echo ">>>>>>>>>> CLEAN UP"

curl -X POST http://localhost:9201/partycontact/cleanup
echo "         [OK] CLEAN UP PARTYCONTACT ENVIRONMENT"


echo ">>>>>>>>>> INITIALIZE"

curl -X POST http://localhost:9201/partycontact/initialize
echo "         [OK] PARTYCONTACT INITIALIZATON"

echo " [OK] CLEAN AND INITIALIZATION-PARTYCONTACT PROCESS FINISHED."



