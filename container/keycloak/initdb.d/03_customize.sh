#!/bin/bash

# Located in root of container
. /kc-lib.sh

echo "----------------"
echo "| Create Roles |"
echo "----------------"
KC_ROLE_NAME=group1Leaders
create_role
KC_ROLE_NAME=group2Leaders
create_role
KC_ROLE_NAME=group3Leaders
create_role
KC_ROLE_NAME=testlead
create_role
KC_ROLE_NAME=${KC_RESOURCE}-reviewer
create_role

echo "----------------"
echo "| Create Users |"
echo "----------------"
KC_PASSWORD=password

KC_USERNAME=user1
KC_FIRSTNAME=James
KC_LASTNAME=Johnson
KC_EMAIL=user1@example.com
create_user
KC_ROLE_NAME=${KC_RESOURCE}-user
assign_role

KC_USERNAME=user2
KC_FIRSTNAME=Robert
KC_LASTNAME=Williams
KC_EMAIL=user2@example.com
create_user
KC_ROLE_NAME=${KC_RESOURCE}-user
assign_role

KC_USERNAME=user3
KC_FIRSTNAME=Michael
KC_LASTNAME=Miller
KC_EMAIL=user3@example.com
create_user
KC_ROLE_NAME=${KC_RESOURCE}-user
assign_role
KC_ROLE_NAME=testlead
assign_role

KC_USERNAME=jdoe
KC_ROLE_NAME=${KC_RESOURCE}-reviewer
assign_role