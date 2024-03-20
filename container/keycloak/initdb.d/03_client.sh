#!/bin/bash

. /lib.sh

echo "-----------------"
echo "| Create Client |"
echo "-----------------"
# KEYCLOAK_RESOURCE set in 00_config.env as it's a shared value
KEYCLOAK_CLIENT_NAME=dtm
KEYCLOAK_SERVICE_ACCOUNT_ENABLED=true
KEYCLOAK_REDIRECT_URIS='["https://localhost:8443/dtm/*"]'
KEYCLOAK_SECRET=yHi6W2raPmLvPXoxqMA7VWbLAA2WN0eB
create_client