alter session set container = XEPDB1;

ALTER SYSTEM SET db_create_file_dest = '/opt/oracle/oradata';

create tablespace DTM;

create user "DTM_OWNER" profile "DEFAULT" identified by "password" default tablespace "DTM" account unlock;

grant connect to DTM_OWNER;
grant unlimited tablespace to DTM_OWNER;

grant create view to DTM_OWNER;
grant create sequence to DTM_OWNER;
grant create table to DTM_OWNER;
grant create procedure to DTM_OWNER;
grant create type to DTM_OWNER;
grant create trigger to DTM_OWNER;