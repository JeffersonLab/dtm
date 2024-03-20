#!/bin/bash

. /lib.sh

echo "----------------"
echo "| Create Roles |"
echo "----------------"
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-user
create_role
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-admin
create_role

KEYCLOAK_ROLE_NAME=group1Leaders
create_role
KEYCLOAK_ROLE_NAME=group2Leaders
create_role
KEYCLOAK_ROLE_NAME=group3Leaders
create_role

KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-reviewer
create_role

echo "----------------"
echo "| Create Users |"
echo "----------------"
KEYCLOAK_USERNAME=jadams
KEYCLOAK_FIRSTNAME=Jane
KEYCLOAK_LASTNAME=Adams
KEYCLOAK_EMAIL=jadams@example.com
KEYCLOAK_PASSWORD=password
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-user
create_user
assign_role

KEYCLOAK_USERNAME=jsmith
KEYCLOAK_FIRSTNAME=John
KEYCLOAK_LASTNAME=Smith
KEYCLOAK_EMAIL=jsmith@example.com
create_user
assign_role


KEYCLOAK_USERNAME=tbrown
KEYCLOAK_FIRSTNAME=Tom
KEYCLOAK_LASTNAME=Brown
KEYCLOAK_EMAIL=tbrown@example.com
create_user
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-user
assign_role
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-admin
assign_role
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-reviewer
assign_role

KEYCLOAK_USERNAME=user1
KEYCLOAK_FIRSTNAME=James
KEYCLOAK_LASTNAME=Johnson
KEYCLOAK_EMAIL=user1@example.com
create_user
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-user
assign_role

KEYCLOAK_USERNAME=user2
KEYCLOAK_FIRSTNAME=Robert
KEYCLOAK_LASTNAME=Williams
KEYCLOAK_EMAIL=user2@example.com
create_user
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-user
assign_role

KEYCLOAK_USERNAME=user3
KEYCLOAK_FIRSTNAME=Michael
KEYCLOAK_LASTNAME=Miller
KEYCLOAK_EMAIL=user3@example.com
create_user
KEYCLOAK_ROLE_NAME=${KEYCLOAK_RESOURCE}-user
assign_role