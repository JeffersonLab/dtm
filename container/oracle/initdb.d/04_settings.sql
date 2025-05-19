alter session set container = XEPDB1;

-- SETTING CONFIG
-- Update with your schema name
ALTER SESSION SET CURRENT_SCHEMA = DTM_OWNER;

-- REQUIRED: Admin Config
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('ADMIN_ROLE_NAME', 'dtm-admin', 'STRING', 'App-specific Admin Role Name', 'AUTH', 1);

-- OPTIONAL: CDN Config
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('SMOOTHNESS_CDN_ENABLED', 'N', 'BOOLEAN', 'Smoothness weblib resources from CDN.  Defaults to No = serve files locally. CDN is for minified files on shared Content Delivery Network (CDN) server - Nice for when multiple apps use same resources to have warm cache.', 'CDN', 1);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('SMOOTHNESS_SERVER', 'ace.jlab.org/cdn', 'STRING', 'Host name and port of content delivery network host for shared smoothness resources. Only used if SMOOTHNESS_CDN_ENABLED = Y', 'CDN', 2);

-- OPTIONAL: IP READ FILTER (ACL)
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('IP_READ_FILTER_ENABLED', 'N', 'BOOLEAN', 'Whether to enable IP filtering of pages requiring auth to view.  You must redeploy the app for this setting change to take effect.', 'ACL', 1);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT, CHANGE_ACTION_JNDI) values ('IP_READ_ALLOWLIST_PATTERN', '127\.0.*', 'STRING', 'Java REGEX Pattern of allowed IPs for unauthenticated access to view IpReadFilter pages', 'ACL', 2, 'java:global/smoothness/IpReadAllowlistReconfigureService');
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('IP_READ_URL_PATTERN', '/*', 'STRING', 'URL patterns to match when applying the IP Read Filter.  You must redeploy the app for this setting change to take effect.', 'ACL', 3);

-- OPTIONAL: Notification Banner
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('NOTIFICATION_ENABLED', 'Y', 'BOOLEAN', 'Notification banner enabled', 'NOTIFICATION', 1);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('NOTIFICATION_MESSAGE', 'Development Environment', 'STRING', 'Notification message', 'NOTIFICATION', 2);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('NOTIFICATION_LINK_NAME', 'Home', 'STRING', 'Notification link name', 'NOTIFICATION', 3);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('NOTIFICATION_LINK_URL', 'https://github.com/JeffersonLab/dtm', 'STRING', 'Notification link URL', 'NOTIFICATION', 4);

-- OPTIONAL: Help Page
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('HELP_CONTENT_CONTACT', 'John Doe (jdoe)', 'STRING', 'Content Contact for help page', 'HELP', 1);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('HELP_TECHNICAL_CONTACT', 'John Doe (jdoe)', 'STRING', 'Technical Contact for help page', 'HELP', 2);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('HELP_DOC_CSV', 'https://github.com/JeffersonLab/dtm|Home', 'CSV', 'CSV of documentation items where each item is a URL and a Label separated with the pipe symbol', 'HELP', 3);

-- OPTIONAL: Email
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT, CHANGE_ACTION_JNDI) values ('EMAIL_ENABLED', 'N', 'BOOLEAN', 'Email integration enabled?', 'EMAIL', 0, 'java:global/dtm/ScheduledEmailer');
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('EMAIL_TESTING_ENABLED', 'N', 'BOOLEAN', 'Send all emails to testlead user group', 'EMAIL', 2);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('EMAIL_DOMAIN_NAME', '@jlab.org', 'STRING', 'The email domain to append to usernames, starting with and including the ampersat.', 'EMAIL', 3);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('EMAIL_SENDER_ADDRESS', 'dtm@jlab.org', 'STRING', 'Email address to use as sender from emails generated in this app.  Note: this is not the same as "from".', 'EMAIL', 4);

-- DTM Specific
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('SMOOTHNESS_VERSION', '4.11.0', 'STRING', 'Version of smoothness lib on CDN.  Only used if SMOOTHNESS_LOCATION=CDN', 'CDN', 3);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('LOGBOOK_ENABLED', 'N', 'BOOLEAN', 'Logbook integration enabled?', 'LOGBOOK', 0);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('EMAIL_EXPERT_CC_LIST', 'tbrown@example.com,someoneelse@example.com', 'CSV', 'List of email addresses to CC when expert emails go out', 'EMAIL', 1);
insert into SETTING (KEY, VALUE, TYPE, DESCRIPTION, TAG, WEIGHT) values ('LOGBOOK_LIST', 'TLOG', 'CSV', 'List of Logbooks to send to when generating log entries', 'LOGBOOK', 1);

