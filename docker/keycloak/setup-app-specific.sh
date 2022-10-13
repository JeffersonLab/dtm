#!/bin/bash

echo "-------------------------------------"
echo "| Step I: Create Group Leader Roles |"
echo "-------------------------------------"
${KEYCLOAK_HOME}/bin/kcadm.sh create roles -r "${KEYCLOAK_REALM}" -s name=group1Leaders
${KEYCLOAK_HOME}/bin/kcadm.sh create roles -r "${KEYCLOAK_REALM}" -s name=group2Leaders
${KEYCLOAK_HOME}/bin/kcadm.sh create roles -r "${KEYCLOAK_REALM}" -s name=group3Leaders

echo "-------------------------"
echo "| Step II: Create Users |"
echo "-------------------------"
${KEYCLOAK_HOME}/bin/kcadm.sh create users -r "${KEYCLOAK_REALM}" -s username=user1 -s firstName=James -s lastName=Johnson -s email=user1@example.com -s enabled=true
${KEYCLOAK_HOME}/bin/kcadm.sh set-password -r "${KEYCLOAK_REALM}" --username user1 --new-password password
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r "${KEYCLOAK_REALM}" --uusername user1 --rolename ${KEYCLOAK_RESOURCE}-user

${KEYCLOAK_HOME}/bin/kcadm.sh create users -r "${KEYCLOAK_REALM}" -s username=user2 -s firstName=Robert -s lastName=Williams -s email=user2@example.com -s enabled=true
${KEYCLOAK_HOME}/bin/kcadm.sh set-password -r "${KEYCLOAK_REALM}" --username user2 --new-password password
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r "${KEYCLOAK_REALM}" --uusername user2 --rolename ${KEYCLOAK_RESOURCE}-user

${KEYCLOAK_HOME}/bin/kcadm.sh create users -r "${KEYCLOAK_REALM}" -s username=user3 -s firstName=Michael -s lastName=Miller -s email=user3@example.com -s enabled=true
${KEYCLOAK_HOME}/bin/kcadm.sh set-password -r "${KEYCLOAK_REALM}" --username user3 --new-password password
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r "${KEYCLOAK_REALM}" --uusername user3 --rolename ${KEYCLOAK_RESOURCE}-user

echo "----------------------------"
echo "| Step III: Assign Leaders |"
echo "----------------------------"
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r "${KEYCLOAK_REALM}" --uusername user1 --rolename group1Leaders
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r "${KEYCLOAK_REALM}" --uusername user2 --rolename group2Leaders
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r "${KEYCLOAK_REALM}" --uusername user3 --rolename group3Leaders
