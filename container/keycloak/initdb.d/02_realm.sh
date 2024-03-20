#!/bin/bash

. /lib.sh

echo "----------------"
echo "| Create Realm |"
echo "----------------"
# KEYCLOAK_REALM set in 00_config.env as it's a shared value
KEYCLOAK_SECRET=yHi6W2raPmLvPXoxqMA7VWbLAA2WN0eB
KEYCLOAK_REALM_DISPLAY_NAME="TEST REALM"
# TIMEOUT UNITS IS SECONDS; 28800 Seconds = 8 Hours
KEYCLOAK_SESSION_IDLE_TIMEOUT=28800
# 86400 Seconds = 24 Hours
KEYCLOAK_SESSION_MAX_LIFESPAN=86400
create_realm