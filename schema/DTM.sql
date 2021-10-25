
CREATE TABLESPACE DTM;

CREATE USER "DTM_OWNER" PROFILE "DEFAULT" IDENTIFIED BY "***" DEFAULT TABLESPACE "DTM" ACCOUNT UNLOCK;

grant connect to DTM_OWNER;
grant create view to DTM_OWNER;
grant create sequence to DTM_OWNER;
grant create table to DTM_OWNER;
grant unlimited tablespace to DTM_OWNER; 
grant create procedure to DTM_OWNER;
grant create type to DTM_OWNER;
grant create trigger to DTM_OWNER;

-- BTM Time Calendar
grant select on event_first_incident to jbta_owner;
grant execute on interval_to_seconds to jbta_owner;

-- DTM to BTM link

grant select on jbta_owner.op_acc_hour to dtm_owner;

DROP SEQUENCE CATEGORY_MONTHLY_GOAL_ID;

CREATE SEQUENCE CATEGORY_MONTHLY_GOAL_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE EVENT_ID;

CREATE SEQUENCE EVENT_ID;

DROP SEQUENCE FSD_DEVICE_EXCEPTION_ID;

CREATE SEQUENCE FSD_DEVICE_EXCEPTION_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE FSD_FAULT_ID;

CREATE SEQUENCE FSD_FAULT_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE FSD_TRIP_ID;

CREATE SEQUENCE FSD_TRIP_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE HIBERNATE_SEQUENCE;

CREATE SEQUENCE HIBERNATE_SEQUENCE;

DROP SEQUENCE INCIDENT_ID;

CREATE SEQUENCE INCIDENT_ID;

DROP SEQUENCE INCIDENT_REPAIR_ID;

CREATE SEQUENCE INCIDENT_REPAIR_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE INCIDENT_REVIEW_ID;

CREATE SEQUENCE INCIDENT_REVIEW_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE NOTE_ID;

CREATE SEQUENCE NOTE_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP SEQUENCE SYSTEM_EXPERT_ID;

CREATE SEQUENCE SYSTEM_EXPERT_ID
	INCREMENT BY 1
	START WITH 1
	NOCYCLE
	NOCACHE
	ORDER;

DROP TABLE DTM_SETTINGS CASCADE CONSTRAINTS PURGE;

DROP TABLE INCIDENT_AUD CASCADE CONSTRAINTS PURGE;

DROP TABLE EVENT_AUD CASCADE CONSTRAINTS PURGE;

DROP TABLE APPLICATION_REVISION_INFO CASCADE CONSTRAINTS PURGE;

DROP TABLE FSD_DEVICE_EXCEPTION CASCADE CONSTRAINTS PURGE;

DROP TABLE FSD_FAULT CASCADE CONSTRAINTS PURGE;

DROP TABLE FSD_TRIP CASCADE CONSTRAINTS PURGE;

DROP TABLE INCIDENT_REPAIR CASCADE CONSTRAINTS PURGE;

DROP TABLE CATEGORY_MONTHLY_GOAL CASCADE CONSTRAINTS PURGE;

DROP TABLE MONTHLY_NOTE CASCADE CONSTRAINTS PURGE;

DROP TABLE INCIDENT_REVIEW CASCADE CONSTRAINTS PURGE;

DROP TABLE INCIDENT CASCADE CONSTRAINTS PURGE;

DROP TABLE EVENT CASCADE CONSTRAINTS PURGE;

DROP TABLE EVENT_TYPE CASCADE CONSTRAINTS PURGE;

DROP TABLE SYSTEM_EXPERT CASCADE CONSTRAINTS PURGE;

CREATE TABLE SYSTEM_EXPERT
(
	SYSTEM_EXPERT_ID     INTEGER NOT NULL ,
	SYSTEM_ID            INTEGER NOT NULL ,
	STAFF_ID             INTEGER NOT NULL ,
CONSTRAINT  SYSTEM_EXPERT_PK PRIMARY KEY (SYSTEM_EXPERT_ID),CONSTRAINT  SYSTEM_EXPERT_AK1 UNIQUE (SYSTEM_ID,STAFF_ID)
);

CREATE TABLE EVENT_TYPE
(
	EVENT_TYPE_ID        INTEGER NOT NULL ,
	NAME                 VARCHAR2(32 CHAR) NOT NULL ,
	ABBREVIATION         VARCHAR2(3 CHAR) NULL ,
	WEIGHT               INTEGER NOT NULL ,
CONSTRAINT  EVENT_TYPE_PK PRIMARY KEY (EVENT_TYPE_ID)
);

CREATE TABLE EVENT
(
	EVENT_ID             INTEGER NOT NULL ,
	TIME_UP              TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
	EVENT_TYPE_ID        INTEGER NOT NULL ,
	TITLE                VARCHAR2(128 CHAR) NOT NULL ,
CONSTRAINT  EVENT_PK PRIMARY KEY (EVENT_ID),CONSTRAINT  EVENT_AK1 UNIQUE (TIME_UP,EVENT_TYPE_ID),
CONSTRAINT EVENT_FK1 FOREIGN KEY (EVENT_TYPE_ID) REFERENCES EVENT_TYPE (EVENT_TYPE_ID) ON DELETE SET NULL
);

CREATE TABLE INCIDENT
(
	INCIDENT_ID          INTEGER NOT NULL ,
	EVENT_ID             INTEGER NOT NULL ,
	TIME_DOWN            TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL ,
	TIME_UP              TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
	TITLE                VARCHAR2(128 CHAR) NOT NULL ,
	SYSTEM_ID            INTEGER NOT NULL ,
	COMPONENT_ID         INTEGER NOT NULL ,
	MISSING_EXPLANATION  VARCHAR2(2048 CHAR) NULL ,
	SUMMARY              VARCHAR2(2048 CHAR) NOT NULL ,
	RESOLUTION           VARCHAR2(2048 CHAR) NULL ,
	REVIEWED_BY          INTEGER NULL ,
	ROOT_CAUSE           VARCHAR2(512 CHAR) NULL ,
	RAR_ID               INTEGER NULL ,
	EXPERT_ACKNOWLEDGED  CHAR(1 BYTE) DEFAULT  'N'  NOT NULL  CONSTRAINT  INCIDENT_CK1 CHECK (EXPERT_ACKNOWLEDGED IN ('N', 'Y', 'R')),
	RAR_EXT              VARCHAR(12) NULL ,
	RAR_UPLOADED_DATE    TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
CONSTRAINT  INCIDENT_PK PRIMARY KEY (INCIDENT_ID),
CONSTRAINT INCIDENT_FK1 FOREIGN KEY (EVENT_ID) REFERENCES EVENT (EVENT_ID) ON DELETE CASCADE
);

CREATE TABLE INCIDENT_REVIEW
(
	INCIDENT_REVIEW_ID   INTEGER NOT NULL ,
	INCIDENT_ID          INTEGER NOT NULL ,
	REVIEWER_ID          INTEGER NOT NULL ,
CONSTRAINT  INCIDENT_REVIEW_PK PRIMARY KEY (INCIDENT_REVIEW_ID),CONSTRAINT  INCIDENT_REVIEWER_AK1 UNIQUE (INCIDENT_ID,REVIEWER_ID),
CONSTRAINT INCIDENT_REVIEW_FK1 FOREIGN KEY (INCIDENT_ID) REFERENCES INCIDENT (INCIDENT_ID)
);

CREATE TABLE MONTHLY_NOTE
(
	NOTE_ID              INTEGER NOT NULL ,
	MONTH                DATE NOT NULL  CONSTRAINT  MONTHLY_NOTE_CK1 CHECK ((TRUNC(MONTH) = MONTH AND EXTRACT(DAY FROM MONTH) = 1)),
	NOTE                 VARCHAR2(3500 CHAR) NULL ,
	MACHINE_GOAL         FLOAT NULL ,
	TRIP_GOAL            FLOAT NULL ,
	EVENT_GOAL           FLOAT NULL ,
CONSTRAINT  MONTHLY_NOTE_PK PRIMARY KEY (NOTE_ID),CONSTRAINT  MONTHLY_NOTE_AK1 UNIQUE (MONTH)
);

CREATE TABLE CATEGORY_MONTHLY_GOAL
(
	GOAL_ID              INTEGER NOT NULL ,
	CATEGORY_ID          INTEGER NOT NULL ,
	MONTH                DATE NOT NULL  CONSTRAINT  CATEGORY_MONTHLY_CK1 CHECK ((TRUNC(MONTH) = MONTH AND EXTRACT(DAY FROM MONTH) = 1)),
	GOAL                 FLOAT NULL ,
CONSTRAINT  CATEGORY_MONTHLY_GOAL_PK PRIMARY KEY (GOAL_ID),CONSTRAINT  CATEGORY_MONTHLY_GOAL_AK1 UNIQUE (CATEGORY_ID,MONTH)
);

CREATE TABLE INCIDENT_REPAIR
(
	INCIDENT_REPAIR_ID   INTEGER NOT NULL ,
	INCIDENT_ID          INTEGER NOT NULL ,
	REPAIRED_BY          INTEGER NOT NULL ,
CONSTRAINT  INCIDENT_REPAIR_PK PRIMARY KEY (INCIDENT_REPAIR_ID),CONSTRAINT  INCIDENT_REPAIR_AK1 UNIQUE (INCIDENT_ID,REPAIRED_BY),
CONSTRAINT INCIDENT_REPAIR_FK1 FOREIGN KEY (INCIDENT_ID) REFERENCES INCIDENT (INCIDENT_ID) ON DELETE CASCADE
);

CREATE TABLE FSD_TRIP
(
	FSD_TRIP_ID          INTEGER NOT NULL ,
	START_UTC            DATE NOT NULL ,
	END_UTC              DATE NULL ,
	RESTORE_UTC          DATE NULL ,
	ACC_STATE            VARCHAR2(16 CHAR) NULL  CONSTRAINT  FSD_TRIP_CK1 CHECK (ACC_STATE IN ('NULL', 'OFF', 'DOWN', 'ACC', 'RESTORE', 'MD')),
	HLA_STATE            VARCHAR2(16 CHAR) NULL  CONSTRAINT  FSD_TRIP_CK2 CHECK (HLA_STATE IN ('OFF', 'DOWN', 'TUNE', 'BANU', 'UP')),
	HLB_STATE            VARCHAR2(16 CHAR) NULL  CONSTRAINT  FSD_TRIP_CK3 CHECK (HLB_STATE IN ('OFF', 'DOWN', 'TUNE', 'BANU', 'UP')),
	HLC_STATE            VARCHAR2(16 CHAR) NULL  CONSTRAINT  FSD_TRIP_CK4 CHECK (HLC_STATE IN ('OFF', 'DOWN', 'TUNE', 'BANU', 'UP')),
	HLD_STATE            VARCHAR2(16 CHAR) NULL  CONSTRAINT  FSD_TRIP_CK5 CHECK (HLD_STATE IN ('OFF', 'DOWN', 'TUNE', 'BANU', 'UP')),
	CAUSE                VARCHAR2(24) NULL ,
	AREA                 VARCHAR2(24) NULL ,
CONSTRAINT  FSD_TRIP_PK PRIMARY KEY (FSD_TRIP_ID),
CONSTRAINT FSD_TRIP_CK6 CHECK ( END_UTC is NULL or (START_UTC <= END_UTC and (RESTORE_UTC is NULL or END_UTC <= RESTORE_UTC)) )
);

CREATE TABLE FSD_FAULT
(
	FSD_FAULT_ID         INTEGER NOT NULL ,
	FSD_TRIP_ID          INTEGER NOT NULL ,
	NODE                 VARCHAR2(64 CHAR) NOT NULL ,
	CHANNEL              INTEGER NULL ,
	DISJOINT_YN          CHAR BYTE NOT NULL  CONSTRAINT  FSD_FAULT_CK1 CHECK (DISJOINT_YN IN ('Y', 'N')),
CONSTRAINT  FSD_FAULT_PK PRIMARY KEY (FSD_FAULT_ID),
CONSTRAINT FSD_FAULT_FK1 FOREIGN KEY (FSD_TRIP_ID) REFERENCES FSD_TRIP (FSD_TRIP_ID) ON DELETE SET NULL
);

CREATE TABLE FSD_DEVICE_EXCEPTION
(
	FSD_DEVICE_EXCEPTION_ID INTEGER NOT NULL ,
	FSD_FAULT_ID         INTEGER NOT NULL ,
	CED_NAME             VARCHAR2(64 CHAR) NOT NULL ,
	CED_TYPE             VARCHAR2(64 CHAR) NOT NULL ,
	HCO_SYSTEM_NAME      VARCHAR2(128 CHAR) NULL ,
	FAULT_CONFIRMATION_YN CHAR DEFAULT  'N'  NOT NULL  CONSTRAINT  BOOLEAN_YN CHECK (FAULT_CONFIRMATION_YN IN ('Y', 'N')),
CONSTRAINT  FSD_DEVICE_EXCEPTION_PK PRIMARY KEY (FSD_DEVICE_EXCEPTION_ID),CONSTRAINT  FSD_DEVICE_EXCEPTION_AK1 UNIQUE (FSD_FAULT_ID,CED_NAME),
CONSTRAINT FSD_DEVICE_EXCEPTION_FK1 FOREIGN KEY (FSD_FAULT_ID) REFERENCES FSD_FAULT (FSD_FAULT_ID) ON DELETE SET NULL
);

CREATE TABLE APPLICATION_REVISION_INFO
(
	REV                  INTEGER NOT NULL ,
	REVTSTMP             INTEGER NOT NULL ,
	USERNAME             VARCHAR2(64 CHAR) NULL ,
	ADDRESS              VARCHAR2(64 CHAR) NULL ,
CONSTRAINT  APPLICATION_REVISION_INFO_PK PRIMARY KEY (REV)
);

CREATE TABLE EVENT_AUD
(
	EVENT_ID             INTEGER NOT NULL ,
	REV                  INTEGER NOT NULL ,
	REVTYPE              INTEGER NOT NULL ,
	TIME_UP              TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
	EVENT_TYPE_ID        INTEGER NOT NULL ,
	TITLE                VARCHAR2(128 CHAR) NOT NULL ,
CONSTRAINT  EVENT_AUD_PK PRIMARY KEY (EVENT_ID,REV),
CONSTRAINT EVENT_AUD_FK1 FOREIGN KEY (REV) REFERENCES APPLICATION_REVISION_INFO (REV)
);

CREATE TABLE INCIDENT_AUD
(
	INCIDENT_ID          INTEGER NOT NULL ,
	REV                  INTEGER NOT NULL ,
	REVTYPE              INTEGER NOT NULL ,
	EVENT_ID             INTEGER NOT NULL ,
	TIME_DOWN            TIMESTAMP(0) WITH LOCAL TIME ZONE NOT NULL ,
	TIME_UP              TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
	TITLE                VARCHAR2(128 CHAR) NOT NULL ,
	SYSTEM_ID            INTEGER NOT NULL ,
	COMPONENT_ID         INTEGER NULL ,
	MISSING_EXPLANATION  VARCHAR2(2048 CHAR) NULL ,
	SUMMARY              VARCHAR2(2048 CHAR) NULL ,
	RESOLUTION           VARCHAR2(2048 CHAR) NULL ,
	REVIEWED_BY          INTEGER NULL ,
	ROOT_CAUSE           VARCHAR2(512 CHAR) NULL ,
	RAR_ID               INTEGER NULL ,
	EXPERT_ACKNOWLEDGED  CHAR(1) DEFAULT  'N'  NOT NULL  CONSTRAINT  INCIDENT_AUD_CK1 CHECK (EXPERT_ACKNOWLEDGED IN ('N', 'Y', 'R')),
	RAR_EXT              VARCHAR(12 CHAR) NULL ,
	RAR_UPLOADED_DATE    TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
CONSTRAINT  INCIDENT_AUD_PK PRIMARY KEY (INCIDENT_ID,REV),
CONSTRAINT INCIDENT_AUD_FK1 FOREIGN KEY (REV) REFERENCES APPLICATION_REVISION_INFO (REV)
);

CREATE TABLE DTM_SETTINGS
(
	DTM_SETTINGS_ID      INTEGER NOT NULL ,
	BULLETIN_BOARD_PATH  VARCHAR2(1024 CHAR) NULL ,
	BULLETIN_BOARD_CATEGORY VARCHAR2(128 CHAR) NULL ,
	AUTO_EMAIL_YN        CHAR(1 BYTE) DEFAULT  'N'  NOT NULL  CONSTRAINT  DTM_SETTINGS_CK1 CHECK (AUTO_EMAIL_YN IN ('Y', 'N')),
CONSTRAINT  DTM_SETTINGS_PK PRIMARY KEY (DTM_SETTINGS_ID)
);

-- Views
create or replace view event_time_down as 
(
select 
e.event_id,
e.event_type_id,
e.time_up,
(select min(i.time_down) from incident i where i.event_id = e.event_id) as time_down
from event e
);

create or replace view open_event as
(
select 
e.EVENT_ID,
t.NAME as EVENT_TYPE,
e.TIME_UP,
e.TIME_DOWN,
t.WEIGHT
from 
event_type t 
left outer join event_time_down e 
on e.event_type_id = t.event_type_id
where e.time_up is null
);

create or replace view event_first_incident (event_id, event_title, time_up, event_type_id, incident_id, title, time_down, number_of_incidents) as (
select e.event_id, e.title, e.time_up, e.event_type_id, i.incident_id, i.title, i.time_down, c.number_of_incidents from event e, (
select * from (
select event_id, incident_id, title, time_down, row_number() over (partition by event_id order by time_down asc) rn from incident
) where rn = 1
) i, (
select event_id, count(*) as number_of_incidents from incident group by event_id) c where e.event_id = i.event_id and e.event_id = c.event_id
);

create or replace view restore_time (event_id, time_down, time_up) as (
SELECT * FROM (SELECT event_id, MAX(nvl(time_up, sysdate)) OVER (partition by event_id ORDER BY time_down) as time_down, LEAD(time_down) OVER (partition by event_id ORDER BY time_down) as time_up FROM incident) WHERE time_down < time_up
union
select a.event_id, b.time_up as time_down, nvl(a.time_up, sysdate) as time_up
from event a,
(select event_id, max(nvl(time_up, sysdate)) as time_up from incident group by event_id) b
where a.event_id = b.event_id
and b.time_up != nvl(a.time_up, sysdate)
);

--create or replace view SYSTEM_ALPHA_CATEGORY as
--SELECT system_id,
--  (SELECT category_id
--  FROM category
--  WHERE parent_id          = 1
--    START WITH category_id = b.category_id
--    CONNECT BY category_id = prior parent_id
--  ) AS "CATEGORY_ID"
--FROM hco_owner.system b;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_FACILITIES" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, 5 as category_id from (
  SELECT system_id,
  (SELECT category_id
  FROM category
  WHERE parent_id          = 5
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 5 from hco_owner.system where category_id = 5;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_CRYO" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, 4 as category_id from (
  SELECT system_id,
  (SELECT category_id
  FROM category
  WHERE parent_id          = 4
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 4 from hco_owner.system where category_id = 4;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_OTHER" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, 3 as category_id from (
  SELECT system_id,
  (SELECT category_id
  FROM category
  WHERE parent_id          = 3
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null and system_id != 616
union all
select system_id, 3 from hco_owner.system where category_id = 3;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_CEBAF" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, category_id from (
  SELECT system_id,
  (SELECT category_id
  FROM category
  WHERE parent_id          = 1
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 1 from hco_owner.system where category_id = 1;

 CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_TRANSPORT" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, 381 as category_id from (
  SELECT system_id,
  (SELECT category_id
  FROM category
  WHERE parent_id          = 381
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 381 from hco_owner.system where category_id = 381;


 CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_ALPHA_CATEGORY" ("SYSTEM_ID", "CATEGORY_ID") AS 
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_cebaf) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_other) union all
    (select "SYSTEM_ID","CATEGORY_ID" from system_category_transport) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_cryo) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_facilities);



  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_HALLD" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, category_id from (
  SELECT system_id,
  (SELECT 401
  FROM category
  WHERE parent_id          = 401
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 401 from hco_owner.system where category_id = 401;


  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_HALLC" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, category_id from (
  SELECT system_id,
  (SELECT 402
  FROM category
  WHERE parent_id          = 402
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 402 from hco_owner.system where category_id = 402;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_HALLB" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, category_id from (
  SELECT system_id,
  (SELECT 403
  FROM category
  WHERE parent_id          = 403
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 403 from hco_owner.system where category_id = 403;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_HALLA" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, category_id from (
  SELECT system_id,
  (SELECT 404
  FROM category
  WHERE parent_id          = 404
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 404 from hco_owner.system where category_id = 404;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_CATEGORY_LERF" ("SYSTEM_ID", "CATEGORY_ID") AS 
  select system_id, category_id from (
  SELECT system_id,
  (SELECT 2
  FROM category
  WHERE parent_id          = 2
    START WITH category_id = b.category_id
    CONNECT BY category_id = prior parent_id
  ) AS "CATEGORY_ID"
FROM hco_owner.system b
) where category_id is not null
union all
select system_id, 2 from hco_owner.system where category_id = 2;

  CREATE OR REPLACE FORCE VIEW "DTM_OWNER"."SYSTEM_ALPHA_CATEGORY_PLUS" ("SYSTEM_ID", "CATEGORY_ID") AS 
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_cebaf) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_other) union all
    (select "SYSTEM_ID","CATEGORY_ID" from system_category_transport) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_cryo) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_facilities) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_lerf) union all  
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_halla) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_hallb) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_hallc) union all
  (select "SYSTEM_ID","CATEGORY_ID" from system_category_halld);

-- Populate Data
insert into event_type values(1, 'Accelerator', 1);
insert into event_type values(2, 'Hall A', 2);
insert into event_type values(3, 'Hall B', 3);
insert into event_type values(4, 'Hall C', 4);
insert into event_type values(5, 'Hall D', 5);
insert into event_type values(6, 'LERF', 6);

-- Triggers
create or replace trigger verify_incident_date_range 
after insert or update on incident
declare
invalid_count integer;
begin
-- Ensure an incident time down is not after the incident time up
select count(*)
into invalid_count
from incident il
where
il.time_up is not null
and il.time_down is not null
and il.time_down > il.time_up;
if invalid_count > 0 then
raise_application_error(-20000, 'incident time down can not come after time up');
end if;
-- Ensure events do not overlap 
select count(*)
into invalid_count
from incident i1, incident i2,
event e1, event e2
where i1.event_id != i2.event_id
and e1.event_type_id = e2.event_type_id
and e1.event_id = i1.event_id
and e2.event_id = i2.event_id
and i1.time_down < nvl(e2.time_up, sysdate)
and i2.time_down < nvl(e1.time_up, sysdate);
if invalid_count > 0 then
raise_application_error(-20001, 'events overlap');
end if;
-- Ensure an incident is closed if the event is closed
select count(*)
into invalid_count
from incident i1,
event e1
where
e1.event_id = i1.event_id
and e1.time_up is not null
and i1.time_up is null;
if invalid_count > 0 then
raise_application_error(-20002, 'event is closed so incident must be too');
end if;
-- Ensure an incident time up is not after the event time up
select count(*)
into invalid_count
from incident i1,
event e1
where
e1.event_id = i1.event_id
and e1.time_up is not null
and i1.time_up is not null
and i1.time_up > e1.time_up;
if invalid_count > 0 then
raise_application_error(-20003, 'incident time up can not come after the event time up');
end if;
end;


create or replace trigger verify_event_date_range 
after insert or update on event
declare
invalid_count integer;
begin
-- Ensure events do not overlap 
select count(*)
into invalid_count
from incident i1, incident i2,
event e1, event e2
where i1.event_id != i2.event_id
and e1.event_type_id = e2.event_type_id
and e1.event_id = i1.event_id
and e2.event_id = i2.event_id
and i1.time_down < nvl(e2.time_up, sysdate)
and i2.time_down < nvl(e1.time_up, sysdate);
if invalid_count > 0 then
raise_application_error(-20020, 'events overlap');
end if;
end;


-- Functions

create or replace
FUNCTION interval_to_seconds (p_interval INTERVAL DAY TO SECOND) RETURN NUMBER IS
BEGIN
RETURN EXTRACT(DAY FROM p_interval)*86400 + EXTRACT(HOUR FROM p_interval)*3600 + EXTRACT(MINUTE FROM p_interval)*60 + EXTRACT(SECOND FROM p_interval);
END interval_to_seconds;

--- FSD WATCHER USER
create user fsd_watcher profile default identified by "mh7kfhso1-k" default tablespace dtm account unlock; 
-- fG2l)nF8zwYp
grant connect to fsd_watcher;
grant insert on dtm_owner.fsd_trip to fsd_watcher;
grant update on dtm_owner.fsd_trip to fsd_watcher;
grant select on dtm_owner.fsd_trip_id to fsd_watcher;

grant select on dtm_owner.fsd_trip to fsd_watcher;
grant select on dtm_owner.fsd_fault to fsd_watcher;
grant select on dtm_owner.fsd_device_exception to fsd_watcher;

grant insert on dtm_owner.fsd_fault to fsd_watcher;
grant select on dtm_owner.fsd_fault_id to fsd_watcher;

grant insert on dtm_owner.fsd_device_exception to fsd_watcher;
grant update on dtm_owner.fsd_device_exception to fsd_watcher;
grant select on dtm_owner.fsd_device_exception_id to fsd_watcher;


create synonym fsd_watcher.fsd_trip for dtm_owner.fsd_trip;
create synonym fsd_watcher.fsd_fault for dtm_owner.fsd_fault;
create synonym fsd_watcher.fsd_device_exception for dtm_owner.fsd_device_exception;
create synonym fsd_watcher.fsd_trip_id for dtm_owner.fsd_trip_id;
create synonym fsd_watcher.fsd_fault_id for dtm_owner.fsd_fault_id;
create synonym fsd_watcher.fsd_device_exception_id for dtm_owner.fsd_device_exception_id;


--- Report Table Functions
-- histo_row type
CREATE type histo_row
AS
  object
  (
    h_date     DATE,
    h_count    NUMBER,
    h_duration NUMBER,
    h_cat      VARCHAR2(128));
  -- histo_tab type
CREATE type histo_tab
AS
  TABLE OF histo_row;
  -- number_tab type
CREATE OR REPLACE type number_tab
AS
  TABLE OF NUMBER;
  -- csv_2_nums func
CREATE OR REPLACE FUNCTION csv_2_nums(
  p_str IN VARCHAR2)
RETURN number_tab
AS
  l_str LONG DEFAULT p_str || ',';
  l_n NUMBER;
  l_data number_tab := number_tab();
BEGIN
  LOOP
    l_n := instr( l_str, ',' );
    EXIT
  WHEN (NVL(l_n,0) = 0);
    l_data.extend;
    l_data( l_data.count ) := ltrim(rtrim(SUBSTR(l_str,1,l_n-1)));
    l_str                  := SUBSTR( l_str, l_n            +1 );
  END LOOP;
  RETURN l_data;
END;
-- fsd_histo_report func
CREATE OR REPLACE FUNCTION fsd_histo_report(
  h_start DATE,
  h_end   DATE,
  h_interval number, -- num hours
  h_cat   VARCHAR2)
RETURN histo_tab
IS
  h_tab histo_tab;
  tab_i NUMBER := 1;
BEGIN
  h_tab := histo_tab();
  FOR outer_row  IN
  (SELECT h_start + (((level - 1) / 24) * h_interval) AS h_date
  FROM dual
    CONNECT BY level <= (((h_end - h_start)) * 24 ) / h_interval
  )
  LOOP
    FOR inner_row IN
    (SELECT COUNT( c.fsd_device_exception_id)                                                                                                                                                                                                                                                         AS COUNT,
      NVL(SUM(least( NVL( CAST((from_tz(CAST(a.end_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE), sysdate), outer_row.h_date + ((1 / 24) * h_interval)) - greatest( CAST((from_tz(CAST(a.start_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE), outer_row.h_date)) * 24, 0) AS duration
    FROM fsd_trip a
    JOIN fsd_fault b USING (fsd_trip_id)
    JOIN fsd_device_exception c USING (fsd_fault_id)
    JOIN hco_owner.system d
    ON (NVL(c.hco_system_name, 'Unknown/Missing') = d.name)
    JOIN system_alpha_category e USING(system_id)
    JOIN category f
    ON (e.category_id                                                                                                  = f.category_id)
    WHERE CAST((from_tz(CAST(a.start_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE)            < outer_row.h_date + ((1 / 24) * h_interval)
    AND NVL(CAST((from_tz(CAST(a.end_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE), sysdate) >= outer_row.h_date
    AND (h_cat                                                                                                        IS NULL
    OR e.category_id                                                                                                  IN (select * from table(csv_2_nums(h_cat))))
    )
    LOOP
      h_tab.extend;
      h_tab(tab_i) := histo_row(outer_row.h_date, inner_row.count, inner_row.duration, '');
      tab_i        := tab_i + 1;
    END LOOP;
  END LOOP;
  RETURN h_tab;
END;
-- fsd_histo_group_report func
CREATE OR REPLACE FUNCTION fsd_histo_group_report(
  h_start DATE,
  h_end   DATE,
  h_interval number,
  h_cat   VARCHAR2)
RETURN histo_tab
IS
  h_tab histo_tab;
  tab_i NUMBER := 1;
BEGIN
  h_tab := histo_tab();
  FOR outer_row  IN
  (SELECT h_start + (((level - 1) / 24) * h_interval) AS h_date
  FROM dual
    CONNECT BY level <= (((h_end - h_start)) * 24 ) / h_interval
  )
  LOOP
    FOR inner_row IN
    (select sum(v_count) as v_count, sum(v_duration) as v_duration, v_category from (
    (SELECT COUNT( c.fsd_device_exception_id)                                                                                                                                                                                                                                                        AS v_count,
      NVL(SUM(least( NVL( CAST((from_tz(CAST(a.end_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE), sysdate), outer_row.h_date + ((1 / 24) * h_interval)) - greatest( CAST((from_tz(CAST(a.start_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE), outer_row.h_date)) * 24, 0) AS v_duration,
      f.name as v_category
    FROM fsd_trip a
    JOIN fsd_fault b USING (fsd_trip_id)
    JOIN fsd_device_exception c USING (fsd_fault_id)
    JOIN hco_owner.system d
    ON (NVL(c.hco_system_name, 'Unknown/Missing') = d.name)
    JOIN system_alpha_category e USING(system_id)
    JOIN category f
    ON (e.category_id                                                                                                  = f.category_id)
    WHERE CAST((from_tz(CAST(a.start_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE)            < outer_row.h_date + ((1 / 24) * h_interval)
    AND NVL(CAST((from_tz(CAST(a.end_utc AS TIMESTAMP), '+00:00') at TIME zone 'America/New_York') AS DATE), sysdate) >= outer_row.h_date
    AND (h_cat                                                                                                        IS NULL
    OR e.category_id                                                                                                  IN (select * from table(csv_2_nums(h_cat))))
    group by f.name
    ) union all (select 0 as v_count, 0 as v_duration, v_category from (select distinct(y.name) as v_category from system_alpha_category x join category y using (category_id)))) group by v_category)
    LOOP
      h_tab.extend;
      h_tab(tab_i) := histo_row(outer_row.h_date, inner_row.v_count, inner_row.v_duration, inner_row.v_category);
      tab_i        := tab_i + 1;
    END LOOP;
  END LOOP;
  RETURN h_tab;
END;
-- incident_histo_report func
CREATE OR REPLACE FUNCTION incident_histo_report(
  h_start DATE,
  h_end   DATE,
  h_interval number, -- num hours
  h_cat   VARCHAR2,
  h_type number,
  h_transport varchar2)
RETURN histo_tab
IS
  h_tab histo_tab;
  tab_i NUMBER := 1;
BEGIN
  h_tab := histo_tab();
  FOR outer_row  IN
  (SELECT h_start + (((level - 1) / 24) * h_interval) AS h_date
  FROM dual
    CONNECT BY level <= (((h_end - h_start)) * 24 ) / h_interval
  )
  LOOP
    FOR inner_row IN
    (SELECT COUNT( a.incident_id)                                                                                                                                                                                                                                                         AS COUNT,
      NVL(SUM(interval_to_seconds(least( NVL(a.time_up, sysdate), outer_row.h_date + ((1 / 24) * h_interval)) - greatest(a.time_down, outer_row.h_date))) / 3600, 0) AS duration
    FROM incident a
    JOIN event b USING (event_id)
    JOIN system_alpha_category c USING (system_id)
    JOIN category d using (category_id)
    WHERE a.time_down < outer_row.h_date + ((1 / 24) * h_interval)
    AND NVL(a.time_up, sysdate) >= outer_row.h_date
    AND (h_cat                                                                                                        IS NULL
    OR category_id                                                                                                  IN (select * from table(csv_2_nums(h_cat))))
    and (h_type is null or b.event_type_id = h_type)
    and (h_transport = 'Y' or system_id != (select system_id from hco_owner.system where name = 'Beam Transport'))
    )
    LOOP
      h_tab.extend;
      h_tab(tab_i) := histo_row(outer_row.h_date, inner_row.count, inner_row.duration, '');
      tab_i        := tab_i + 1;
    END LOOP;
  END LOOP;
  RETURN h_tab;
END;
CREATE OR REPLACE FUNCTION incident_histo_group_report(
  h_start DATE,
  h_end   DATE,
  h_interval number, -- num hours
  h_cat   VARCHAR2,
  h_type number,
  h_transport varchar2)
RETURN histo_tab
IS
  h_tab histo_tab;
  tab_i NUMBER := 1;
BEGIN
  h_tab := histo_tab();
  FOR outer_row  IN
  (SELECT h_start + (((level - 1) / 24) * h_interval) AS h_date
  FROM dual
    CONNECT BY level <= (((h_end - h_start)) * 24 ) / h_interval
  )
  LOOP
    FOR inner_row IN
    (select sum(v_count) as v_count, sum(v_duration) as v_duration, v_category from (
    (SELECT COUNT( a.incident_id) as v_count,                                                                                                                                                                                                                                                     
      NVL(SUM(interval_to_seconds(least( NVL(a.time_up, sysdate), outer_row.h_date + ((1 / 24) * h_interval)) - greatest(a.time_down, outer_row.h_date))) / 3600, 0) AS v_duration,
      d.name as v_category
    FROM incident a
    JOIN event b USING (event_id)
    JOIN system_alpha_category c USING (system_id)
    JOIN category d using (category_id)
    WHERE a.time_down < outer_row.h_date + ((1 / 24) * h_interval)
    AND NVL(a.time_up, sysdate) >= outer_row.h_date
    AND (h_cat                                                                                                        IS NULL
    OR category_id                                                                                                  IN (select * from table(csv_2_nums(h_cat))))
    and (h_type is null or b.event_type_id = h_type)
    and (h_transport = 'Y' or system_id != (select system_id from hco_owner.system where name = 'Beam Transport'))
    group by d.name
    ) union all (select 0 as v_count, 0 as v_duration, v_category from (select distinct(y.name) as v_category from system_alpha_category x join category y using (category_id)))) group by v_category)
    LOOP
      h_tab.extend;
      h_tab(tab_i) := histo_row(outer_row.h_date, inner_row.v_count, inner_row.v_duration, inner_row.v_category);
      tab_i        := tab_i + 1;
    END LOOP;
  END LOOP;
  RETURN h_tab;
END;

create or replace view system_packed_incidents as
-- http://blogs.solidq.com/en/sqlserver/packing-intervals/
WITH C0 AS
(
select incident_id, system_id, event_type_id, time_down, nvl(a.time_up, sysdate) as time_up
from incident a inner join event using(event_id)
),
C1 AS
(
SELECT incident_id, system_id, event_type_id, time_down AS ts, +1 AS type, 1 AS sub
FROM C0
UNION ALL
SELECT incident_id, system_id, event_type_id, time_up AS ts, -1 AS type, 0 AS sub
FROM C0
),
C2 AS
(
SELECT C1.*,
SUM(type) OVER(PARTITION BY system_id, event_type_id ORDER BY ts, type DESC
ROWS BETWEEN UNBOUNDED PRECEDING
AND CURRENT ROW) - sub AS cnt
FROM C1
),
C3 AS
(
SELECT incident_id, system_id, event_type_id, ts,
FLOOR((ROW_NUMBER() OVER(PARTITION BY system_id, event_type_id ORDER BY ts) - 1) / 2 + 1)
AS grpnum
FROM C2
WHERE cnt = 0
)
SELECT min(incident_id) as first_incident_id, system_id, event_type_id, MIN(ts) AS time_down, max(ts) AS time_up
FROM C3
GROUP BY system_id, event_type_id, grpnum;







create or replace view alpha_cat_packed_incidents as 
-- http://blogs.solidq.com/en/sqlserver/packing-intervals/
WITH C0 AS
(
select incident_id, category_id, event_type_id, time_down, nvl(a.time_up, sysdate) as time_up 
from incident a inner join event using(event_id) inner join system_alpha_category using(system_id)
),
C1 AS
(
SELECT incident_id, category_id, event_type_id, time_down AS ts, +1 AS type, 1 AS sub
FROM C0
UNION ALL
SELECT incident_id, category_id, event_type_id, time_up AS ts, -1 AS type, 0 AS sub
FROM C0
),
C2 AS
(
SELECT C1.*,
SUM(type) OVER(PARTITION BY category_id, event_type_id ORDER BY ts, type DESC
ROWS BETWEEN UNBOUNDED PRECEDING
AND CURRENT ROW) - sub AS cnt
FROM C1
),
C3 AS
(
SELECT incident_id, category_id, event_type_id, ts,
FLOOR((ROW_NUMBER() OVER(PARTITION BY category_id, event_type_id ORDER BY ts) - 1) / 2 + 1)
AS grpnum
FROM C2
WHERE cnt = 0
)
SELECT min(incident_id) as first_incident_id, category_id, event_type_id, MIN(ts) AS time_down, max(ts) AS time_up
FROM C3
GROUP BY category_id, event_type_id, grpnum;

-- Virtual Columns
alter table incident add duration_seconds number generated always as (
((extract(day from time_up - time_down)) * 86400) +
((extract(hour from time_up - time_down)) * 3600) +
((extract(minute from time_up - time_down)) * 60) +
((extract(second from time_up - time_down)))
) virtual;

alter table incident add reviewed char(1) generated always as ((case when (time_up - time_down) < NUMTODSINTERVAL(30, 'minute') and expert_acknowledged = 'Y' then 'Y' when (time_up - time_down) < NUMTODSINTERVAL(4, 'hour') and root_cause is not null then 'Y' when rar_ext is not null then 'Y' else 'N' end)) virtual;

alter table incident add review_level varchar2(10) generated always as ((case when (time_up - time_down) < NUMTODSINTERVAL(30, 'minute') then 'ONE' when (time_up - time_down) < NUMTODSINTERVAL(4, 'hour') then 'TWO' else 'THREE_PLUS' end)) virtual;


-- Performance Index
create index incident_perf1 on incident(event_id);
