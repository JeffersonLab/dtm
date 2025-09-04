alter session set container = XEPDB1;

--DROP SEQUENCE DTM_OWNER.CATEGORY_MONTHLY_GOAL_ID;
--DROP SEQUENCE DTM_OWNER.EVENT_ID;
--DROP SEQUENCE DTM_OWNER.FSD_DEVICE_EXCEPTION_ID;
--DROP SEQUENCE DTM_OWNER.FSD_FAULT_ID;
--DROP SEQUENCE DTM_OWNER.FSD_TRIP_ID;
--DROP SEQUENCE DTM_OWNER.HIBERNATE_SEQUENCE;
--DROP SEQUENCE DTM_OWNER.INCIDENT_ID;
--DROP SEQUENCE DTM_OWNER.INCIDENT_REPAIR_ID;
--DROP SEQUENCE DTM_OWNER.INCIDENT_REVIEW_ID;
--DROP SEQUENCE DTM_OWNER.NOTE_ID;
--DROP SEQUENCE DTM_OWNER.SYSTEM_EXPERT_ID;

--DROP TABLE DTM_OWNER.INCIDENT_AUD CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.EVENT_AUD CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.APPLICATION_REVISION_INFO CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.FSD_DEVICE_EXCEPTION CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.FSD_FAULT CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.FSD_TRIP CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.INCIDENT_REPAIR CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.CATEGORY_MONTHLY_GOAL CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.MONTHLY_NOTE CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.INCIDENT_REVIEW CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.INCIDENT CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.EVENT CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.EVENT_TYPE CASCADE CONSTRAINTS PURGE;
--DROP TABLE DTM_OWNER.SYSTEM_EXPERT CASCADE CONSTRAINTS PURGE;

CREATE SEQUENCE DTM_OWNER.CATEGORY_MONTHLY_GOAL_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.EVENT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.FSD_DEVICE_EXCEPTION_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.FSD_FAULT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.FSD_TRIP_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.HIBERNATE_SEQUENCE
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.INCIDENT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.INCIDENT_HALL_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE SEQUENCE DTM_OWNER.INCIDENT_REPAIR_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.INCIDENT_REVIEW_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.NOTE_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE DTM_OWNER.SYSTEM_EXPERT_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE TABLE DTM_OWNER.SETTING
(
    KEY                VARCHAR2(128 CHAR)  NOT NULL,
    VALUE              VARCHAR2(4000 BYTE) NOT NULL,
    TYPE               VARCHAR2(32 CHAR) DEFAULT 'STRING' NOT NULL,
    DESCRIPTION        VARCHAR2(2048 CHAR) NOT NULL,
    TAG                VARCHAR2(32 CHAR) DEFAULT 'OTHER' NOT NULL,
    WEIGHT             INTEGER DEFAULT 0 NOT NULL,
    CHANGE_ACTION_JNDI VARCHAR2(128 CHAR) NULL ,
    CONSTRAINT SETTING_PK PRIMARY KEY (KEY),
    CONSTRAINT SETTING_CK1 CHECK (TYPE IN ('STRING', 'BOOLEAN', 'CSV')),
    CONSTRAINT SETTING_CK2 CHECK ((VALUE in ('Y', 'N') AND TYPE = 'BOOLEAN') OR TYPE <> 'BOOLEAN')
);

CREATE TABLE DTM_OWNER.SYSTEM_EXPERT
(
    SYSTEM_EXPERT_ID     INTEGER NOT NULL ,
    SYSTEM_ID            INTEGER NOT NULL ,
    USERNAME             VARCHAR2(64 CHAR) NOT NULL ,
    CONSTRAINT SYSTEM_EXPERT_PK PRIMARY KEY (SYSTEM_EXPERT_ID),
    CONSTRAINT SYSTEM_EXPERT_AK1 UNIQUE (SYSTEM_ID,USERNAME)
);

CREATE TABLE DTM_OWNER.EVENT_TYPE
(
    EVENT_TYPE_ID        INTEGER NOT NULL ,
    NAME                 VARCHAR2(32 CHAR) NOT NULL ,
    DESCRIPTION          VARCHAR2(32 CHAR) NULL,
    ABBREVIATION         VARCHAR2(3 CHAR) NULL ,
    WEIGHT               INTEGER NOT NULL ,
    ARCHIVED_YN          VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
    MULTI_HALL_YN        VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
    CONSTRAINT EVENT_TYPE_PK PRIMARY KEY (EVENT_TYPE_ID),
    CONSTRAINT EVENT_TYPE_CK1 CHECK (ARCHIVED_YN IN ('Y','N'))
);

CREATE TABLE DTM_OWNER.EVENT
(
    EVENT_ID             INTEGER NOT NULL ,
    TIME_UP              TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
    EVENT_TYPE_ID        INTEGER NOT NULL ,
    TITLE                VARCHAR2(128 CHAR) NOT NULL ,
    CONSTRAINT EVENT_PK PRIMARY KEY (EVENT_ID),
    CONSTRAINT EVENT_AK1 UNIQUE (TIME_UP,EVENT_TYPE_ID),
    CONSTRAINT EVENT_FK1 FOREIGN KEY (EVENT_TYPE_ID) REFERENCES DTM_OWNER.EVENT_TYPE (EVENT_TYPE_ID) ON DELETE SET NULL
);

CREATE TABLE DTM_OWNER.INCIDENT
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
    REVIEWED_USERNAME    VARCHAR2(64 CHAR) NULL ,
    ROOT_CAUSE           VARCHAR2(512 CHAR) NULL ,
    RAR_ID               INTEGER NULL ,
    EXPERT_ACKNOWLEDGED  CHAR(1 BYTE) DEFAULT  'N'  NOT NULL  CONSTRAINT  INCIDENT_CK1 CHECK (EXPERT_ACKNOWLEDGED IN ('N', 'Y', 'R')),
	RAR_EXT              VARCHAR(12) NULL ,
	RAR_UPLOADED_DATE    TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
	PERMIT_TO_WORK       VARCHAR2(64 CHAR) NULL ,
    CONSTRAINT INCIDENT_PK PRIMARY KEY (INCIDENT_ID),
    CONSTRAINT INCIDENT_FK1 FOREIGN KEY (EVENT_ID) REFERENCES DTM_OWNER.EVENT (EVENT_ID) ON DELETE CASCADE
);

CREATE TABLE DTM_OWNER.INCIDENT_HALL
(
    INCIDENT_HALL_ID INTEGER NOT NULL,
    INCIDENT_ID      INTEGER NOT NULL,
    HALL             VARCHAR2(1 CHAR) NOT NULL,
    CONSTRAINT INCIDENT_HALL_PK PRIMARY KEY (INCIDENT_HALL_ID),
    CONSTRAINT INCIDENT_HALL_AK1 UNIQUE (INCIDENT_ID, HALL),
    CONSTRAINT INCIDENT_HALL_FK1 FOREIGN KEY (INCIDENT_ID) REFERENCES DTM_OWNER.INCIDENT (INCIDENT_ID) ON DELETE CASCADE,
    CONSTRAINT INCIDENT_HALL_CK1 CHECK (HALL IN ('A','B','C','D'))
);

CREATE TABLE DTM_OWNER.INCIDENT_REVIEW
(
    INCIDENT_REVIEW_ID   INTEGER NOT NULL ,
    INCIDENT_ID          INTEGER NOT NULL ,
    REVIEWER_USERNAME    VARCHAR2(64 CHAR) NOT NULL ,
    CONSTRAINT INCIDENT_REVIEW_PK PRIMARY KEY (INCIDENT_REVIEW_ID),
    CONSTRAINT INCIDENT_REVIEWER_AK1 UNIQUE (INCIDENT_ID,REVIEWER_USERNAME),
    CONSTRAINT INCIDENT_REVIEW_FK1 FOREIGN KEY (INCIDENT_ID) REFERENCES DTM_OWNER.INCIDENT (INCIDENT_ID)
);

CREATE TABLE DTM_OWNER.MONTHLY_NOTE
(
    NOTE_ID              INTEGER NOT NULL ,
    MONTH                DATE NOT NULL  CONSTRAINT  MONTHLY_NOTE_CK1 CHECK ((TRUNC(MONTH) = MONTH AND EXTRACT(DAY FROM MONTH) = 1)),
    NOTE                 VARCHAR2(3500 CHAR) NULL ,
    MACHINE_GOAL         FLOAT NULL ,
    TRIP_GOAL            FLOAT NULL ,
    EVENT_GOAL           FLOAT NULL ,
    CONSTRAINT MONTHLY_NOTE_PK PRIMARY KEY (NOTE_ID),
    CONSTRAINT MONTHLY_NOTE_AK1 UNIQUE (MONTH)
);

CREATE TABLE DTM_OWNER.CATEGORY_MONTHLY_GOAL
(
    GOAL_ID              INTEGER NOT NULL ,
    CATEGORY_ID          INTEGER NOT NULL ,
    MONTH                DATE NOT NULL  CONSTRAINT  CATEGORY_MONTHLY_CK1 CHECK ((TRUNC(MONTH) = MONTH AND EXTRACT(DAY FROM MONTH) = 1)),
    GOAL                 FLOAT NULL ,
    CONSTRAINT CATEGORY_MONTHLY_GOAL_PK PRIMARY KEY (GOAL_ID),
    CONSTRAINT CATEGORY_MONTHLY_GOAL_AK1 UNIQUE (CATEGORY_ID,MONTH)
);

CREATE TABLE DTM_OWNER.INCIDENT_REPAIR
(
    INCIDENT_REPAIR_ID   INTEGER NOT NULL ,
    INCIDENT_ID          INTEGER NOT NULL ,
    REPAIRED_BY          INTEGER NOT NULL ,
    CONSTRAINT INCIDENT_REPAIR_PK PRIMARY KEY (INCIDENT_REPAIR_ID),
    CONSTRAINT INCIDENT_REPAIR_AK1 UNIQUE (INCIDENT_ID,REPAIRED_BY),
    CONSTRAINT INCIDENT_REPAIR_FK1 FOREIGN KEY (INCIDENT_ID) REFERENCES DTM_OWNER.INCIDENT (INCIDENT_ID) ON DELETE CASCADE
);

CREATE TABLE DTM_OWNER.FSD_TRIP
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
    CONSTRAINT FSD_TRIP_PK PRIMARY KEY (FSD_TRIP_ID),
    CONSTRAINT FSD_TRIP_CK6 CHECK ( END_UTC is NULL or (START_UTC <= END_UTC and (RESTORE_UTC is NULL or END_UTC <= RESTORE_UTC)) )
);

CREATE TABLE DTM_OWNER.FSD_FAULT
(
    FSD_FAULT_ID         INTEGER NOT NULL ,
    FSD_TRIP_ID          INTEGER NOT NULL ,
    NODE                 VARCHAR2(64 CHAR) NOT NULL ,
    CHANNEL              INTEGER NULL ,
    DISJOINT_YN          CHAR(1 BYTE) NOT NULL CONSTRAINT FSD_FAULT_CK1 CHECK (DISJOINT_YN IN ('Y', 'N')),
    CONSTRAINT FSD_FAULT_PK PRIMARY KEY (FSD_FAULT_ID),
    CONSTRAINT FSD_FAULT_FK1 FOREIGN KEY (FSD_TRIP_ID) REFERENCES DTM_OWNER.FSD_TRIP (FSD_TRIP_ID) ON DELETE SET NULL
);

CREATE TABLE DTM_OWNER.FSD_DEVICE_EXCEPTION
(
    FSD_DEVICE_EXCEPTION_ID INTEGER NOT NULL ,
    FSD_FAULT_ID         INTEGER NOT NULL ,
    CED_NAME             VARCHAR2(64 CHAR) NOT NULL ,
    CED_TYPE             VARCHAR2(64 CHAR) NOT NULL ,
    HCO_SYSTEM_NAME      VARCHAR2(128 CHAR) NULL ,
    FAULT_CONFIRMATION_YN CHAR DEFAULT  'N'  NOT NULL  CONSTRAINT  BOOLEAN_YN CHECK (FAULT_CONFIRMATION_YN IN ('Y', 'N')),
    CONSTRAINT FSD_DEVICE_EXCEPTION_PK PRIMARY KEY (FSD_DEVICE_EXCEPTION_ID),
    CONSTRAINT FSD_DEVICE_EXCEPTION_AK1 UNIQUE (FSD_FAULT_ID,CED_NAME),
    CONSTRAINT FSD_DEVICE_EXCEPTION_FK1 FOREIGN KEY (FSD_FAULT_ID) REFERENCES DTM_OWNER.FSD_FAULT (FSD_FAULT_ID) ON DELETE SET NULL
);

CREATE TABLE DTM_OWNER.APPLICATION_REVISION_INFO
(
    REV                  INTEGER NOT NULL ,
    REVTSTMP             INTEGER NOT NULL ,
    USERNAME             VARCHAR2(64 CHAR) NULL ,
    ADDRESS              VARCHAR2(64 CHAR) NULL ,
    CONSTRAINT APPLICATION_REVISION_INFO_PK PRIMARY KEY (REV)
);

CREATE TABLE DTM_OWNER.EVENT_AUD
(
    EVENT_ID             INTEGER NOT NULL ,
    REV                  INTEGER NOT NULL ,
    REVTYPE              INTEGER NOT NULL ,
    TIME_UP              TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
    EVENT_TYPE_ID        INTEGER NOT NULL ,
    TITLE                VARCHAR2(128 CHAR) NOT NULL ,
    CONSTRAINT EVENT_AUD_PK PRIMARY KEY (EVENT_ID,REV),
    CONSTRAINT EVENT_AUD_FK1 FOREIGN KEY (REV) REFERENCES DTM_OWNER.APPLICATION_REVISION_INFO (REV)
);

CREATE TABLE DTM_OWNER.INCIDENT_AUD
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
    REVIEWED_USERNAME    VARCHAR2(64 CHAR) NULL ,
    ROOT_CAUSE           VARCHAR2(512 CHAR) NULL ,
    RAR_ID               INTEGER NULL ,
    EXPERT_ACKNOWLEDGED  CHAR(1) DEFAULT 'N' NOT NULL CONSTRAINT INCIDENT_AUD_CK1 CHECK (EXPERT_ACKNOWLEDGED IN ('N', 'Y', 'R')),
    RAR_EXT              VARCHAR(12 CHAR) NULL ,
	RAR_UPLOADED_DATE    TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
	PERMIT_TO_WORK       VARCHAR2(64 CHAR) NULL ,
CONSTRAINT INCIDENT_AUD_PK PRIMARY KEY (INCIDENT_ID,REV),
CONSTRAINT INCIDENT_AUD_FK1 FOREIGN KEY (REV) REFERENCES DTM_OWNER.APPLICATION_REVISION_INFO (REV)
);

CREATE TABLE DTM_OWNER.CATEGORY
(
    CATEGORY_ID NUMBER NOT NULL CONSTRAINT CATEGORY_PK PRIMARY KEY,
    NAME        VARCHAR2(128 CHAR) NOT NULL CONSTRAINT CATEGORY_AK1 UNIQUE,
    PARENT_ID   NUMBER CONSTRAINT CATEGORY_FK1 REFERENCES DTM_OWNER.CATEGORY ON DELETE SET NULL,
    WEIGHT      NUMBER
);

/*grant select on srm_owner.category to dtm_owner;
create or replace view CATEGORY as
(
select distinct category_id, name, parent_id, weight
from srm_owner.category z
start with z.category_id in
           (select category_id
            from srm_owner.system a
            where system_id in
                  (select system_id from srm_owner.system_application where application_id = 2))
connect by prior z.parent_id = z.category_id
);*/

CREATE TABLE DTM_OWNER.SYSTEM
(
    SYSTEM_ID   NUMBER NOT NULL CONSTRAINT SYSTEM_PK PRIMARY KEY,
    NAME        VARCHAR2(128 CHAR) NOT NULL CONSTRAINT SYSTEM_AK1 UNIQUE,
    CATEGORY_ID NUMBER NOT NULL CONSTRAINT SYSTEM_FK1 REFERENCES DTM_OWNER.CATEGORY ON DELETE SET NULL,
    WEIGHT      NUMBER,
    SRM_YN      CHAR(1 BYTE) DEFAULT 'N' NOT NULL CONSTRAINT SYSTEM_CK1 CHECK (SRM_YN IN ('Y', 'N'))
);

/*grant select on srm_owner.system to dtm_owner;
grant select on srm_owner.system_application to dtm_owner;
create or replace view dtm_owner.system as
(
select SYSTEM_ID, NAME, CATEGORY_ID, WEIGHT,
CASE
  WHEN (select 'Y' from srm_owner.system_application where srm_owner.system_application.system_id = b.system_id and application_id = 1) IS NOT NULL THEN 'Y'
  ELSE 'N'
END as SRM_YN
from srm_owner.system b where system_id in (select system_id from srm_owner.system_application where application_id = 2)
);*/

-- Note: Region is used by FSD Trip reports to map to "area"
CREATE TABLE DTM_OWNER.REGION
(
    REGION_ID NUMBER NOT NULL CONSTRAINT REGION_PK PRIMARY KEY,
    NAME      VARCHAR2(128 CHAR) NOT NULL,
    ALIAS     VARCHAR2(128 CHAR),
    WEIGHT    NUMBER
);

/*grant select on srm_owner.region to dtm_owner;
create or replace view dtm_owner.region as
(
select REGION_ID,NAME,ALIAS,WEIGHT from srm_owner.region
);*/

CREATE TABLE DTM_OWNER.COMPONENT
(
    COMPONENT_ID         NUMBER NOT NULL CONSTRAINT COMPONENT_PK PRIMARY KEY,
    NAME                 VARCHAR2(128 char) NOT NULL CONSTRAINT COMPONENT_CK4 CHECK (INSTR(NAME, '*') = 0),
    SYSTEM_ID            NUMBER NOT NULL CONSTRAINT COMPONENT_FK2 REFERENCES DTM_OWNER.SYSTEM ON DELETE SET NULL,
    REGION_ID            NUMBER NOT NULL CONSTRAINT COMPONENT_FK1 REFERENCES DTM_OWNER.REGION,
    CONSTRAINT COMPONENT_AK1 UNIQUE (NAME, SYSTEM_ID),
    CONSTRAINT COMPONENT_AK2 UNIQUE (SYSTEM_ID, COMPONENT_ID)
);

/*grant select on srm_owner.component to dtm_owner;
create or replace view dtm_owner.component as
(
select component_id, name, system_id, region_id from srm_owner.component where system_id in (select system_id from srm_owner.system_application where application_id = 2)
);*/

CREATE TABLE DTM_OWNER.WORKGROUP
(
    WORKGROUP_ID     NUMBER NOT NULL CONSTRAINT WORKGROUP_PK PRIMARY KEY,
    NAME             VARCHAR2(128 CHAR) NOT NULL CONSTRAINT WORKGROUP_AK1 UNIQUE
);

/*grant select on srm_owner.responsible_group to dtm_owner;
create or replace view dtm_owner.workgroup as
(
select group_id as workgroup_id, name from srm_owner.responsible_group
);*/

CREATE TABLE DTM_OWNER.CC_ACC_HOUR
(
    CC_ACC_HOUR_ID  NUMBER                            not null
        constraint CC_ACC_HOUR_PK
            primary key,
    DAY_AND_HOUR    TIMESTAMP(0) WITH LOCAL TIME ZONE not null
        constraint CC_ACC_HOUR_AK1
            unique
        constraint CC_ACC_HOUR_CK1
            check (EXTRACT(MINUTE FROM DAY_AND_HOUR) = 0 AND EXTRACT(SECOND FROM DAY_AND_HOUR) = 0),
    UP_SECONDS      NUMBER(4) default 0               not null
        constraint OP_ACC_HOUR_CK2
            check (UP_SECONDS BETWEEN 0 AND 3600),
    SAD_SECONDS     NUMBER(4) default 0               not null
        constraint OP_ACC_HOUR_CK3
            check (SAD_SECONDS BETWEEN 0 AND 3600),
    DOWN_SECONDS    NUMBER(4) default 0               not null
        constraint OP_ACC_HOUR_CK4
            check (DOWN_SECONDS BETWEEN 0 AND 3600),
    STUDIES_SECONDS NUMBER(4) default 0               not null
        constraint OP_ACC_HOUR_CK5
            check (STUDIES_SECONDS BETWEEN 0 AND 3600),
    ACC_SECONDS     NUMBER(4) default 0               not null
        constraint OP_ACC_HOUR_CK6
            check (ACC_SECONDS BETWEEN 0 AND 3600),
    RESTORE_SECONDS NUMBER(4) default 0               not null
        constraint OP_ACC_HOUR_CK7
            check (RESTORE_SECONDS BETWEEN 0 AND 3600),
    constraint OP_ACC_HOUR_CK8
        check (UP_SECONDS + SAD_SECONDS + DOWN_SECONDS + STUDIES_SECONDS + RESTORE_SECONDS + ACC_SECONDS = 3600)
);

/* grant select on btm_owner.cc_acc_hour to dtm_owner;
create or replace view dtm_owner.cc_acc_hour as
(
select * from btm_owner.cc_acc_hour
);*/

CREATE OR REPLACE VIEW DTM_OWNER.ALL_COMPONENTS as
(
select component_id,
       name,
       system_id,
       region_id
from dtm_owner.component
);

/*grant select on srm_owner.component_aud to dtm_owner;
grant select on srm_owner.application_revision_info to dtm_owner;

CREATE OR REPLACE VIEW DTM_OWNER.ALL_COMPONENTS as
(
select distinct(component_id), name, system_id, region_id
from srm_owner.component_aud inner join srm_owner.application_revision_info using(rev)
where revtype = 2
union
select component_id,
       name,
       system_id,
       region_id
from dtm_owner.component
);*/


CREATE OR REPLACE VIEW DTM_OWNER.ALL_SYSTEMS AS (
       select system_id, name, category_id, weight, srm_yn from dtm_owner.system
);

/*
 * GRANT SELECT ON SRM_OWNER.SYSTEM_AUD TO DTM_OWNER;
create view DTM_OWNER.ALL_SYSTEMS as
(
select distinct(system_id), name, category_id, weight, 'N' as srm_yn
from srm_owner.system_aud
         inner join srm_owner.application_revision_info using (rev)
where revtype = 2
union
select system_id,
       name,
       category_id,
       weight,
       srm_yn
from dtm_owner.system
);
 */


CREATE TABLE DTM_OWNER.TYPE_CATEGORY (
    EVENT_TYPE_ID NUMBER NOT NULL,
    CATEGORY_ID   NUMBER NOT NULL,
    CONSTRAINT TYPE_CATEGORY_PK PRIMARY KEY (EVENT_TYPE_ID,CATEGORY_ID),
    CONSTRAINT TYPE_CATEGORY_FK1 FOREIGN KEY (EVENT_TYPE_ID) REFERENCES DTM_OWNER.EVENT_TYPE (EVENT_TYPE_ID) ON DELETE CASCADE,
    CONSTRAINT TYPE_CATEGORY_FK2 FOREIGN KEY (CATEGORY_ID) REFERENCES DTM_OWNER.CATEGORY (CATEGORY_ID) ON DELETE CASCADE
);

create table DTM_OWNER.ALPHA_CATEGORY (
    CATEGORY_ID NUMBER NOT NULL,
    CONSTRAINT ALPHA_CATEGORY_PK PRIMARY KEY (CATEGORY_ID),
    CONSTRAINT ALPHA_CATEGORY_FK1 FOREIGN KEY (CATEGORY_ID) REFERENCES DTM_OWNER.CATEGORY (CATEGORY_ID)
);

create type dtm_owner.alpha_row
AS
    object
(
    category_id NUMBER,
    system_id NUMBER
)
/

create type dtm_owner.ALPHA_TAB as table of dtm_owner.alpha_row
/

create FUNCTION dtm_owner.generate_alpha_view
    RETURN alpha_tab
    IS
    h_tab alpha_tab;
    tab_i NUMBER := 1;
BEGIN
    h_tab := alpha_tab();
    FOR outer_row  IN
        (
        SELECT category_id from dtm_owner.alpha_category
        )
        LOOP
            FOR inner_row IN
                (
                select outer_row.category_id as category_id, system_id from system where category_id in (SELECT category_id
                                                                                                         FROM category
                                                                                                         START WITH category_id = outer_row.category_id
                                                                                                         CONNECT BY PRIOR category_id = parent_id
                )
                )
                LOOP
                    h_tab.extend;
                    h_tab(tab_i) := alpha_row(outer_row.category_id, inner_row.system_id);
                    tab_i        := tab_i + 1;
                END LOOP;
        END LOOP;
    RETURN h_tab;
END;
/

create view dtm_owner.system_alpha_category as
(
select *
from TABLE (generate_alpha_view())
);


CREATE OR REPLACE VIEW DTM_OWNER.SYSTEM_PACKED_INCIDENTS as
WITH C0 AS
         (
             select incident_id, system_id, event_type_id, time_down, nvl(a.time_up, sysdate) as time_up
             from dtm_owner.incident a inner join dtm_owner.event using(event_id)
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

CREATE OR REPLACE VIEW DTM_OWNER.ALPHA_CAT_PACKED_INCIDENTS as
WITH C0 AS
         (
             select incident_id, category_id, event_type_id, time_down, nvl(a.time_up, sysdate) as time_up
             from dtm_owner.incident a inner join dtm_owner.event using(event_id) inner join dtm_owner.system_alpha_category using(system_id)
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


CREATE OR REPLACE VIEW DTM_OWNER.EVENT_TIME_DOWN AS
(
SELECT
e.EVENT_ID,
e.EVENT_TYPE_ID,
e.TIME_UP,
(SELECT MIN(i.TIME_DOWN) FROM DTM_OWNER.INCIDENT i WHERE i.EVENT_ID = e.EVENT_ID) AS TIME_DOWN
FROM DTM_OWNER.EVENT e
);

CREATE OR REPLACE VIEW DTM_OWNER.EVENT_FIRST_INCIDENT AS
(
SELECT e.event_id, e.title as event_title, e.time_up, e.event_type_id, i.incident_id, i.title, i.time_down, c.number_of_incidents
FROM dtm_owner.event e,
(
SELECT * FROM (
SELECT event_id, incident_id, title, time_down, row_number() over (PARTITION BY event_id ORDER BY time_down ASC) rn
FROM dtm_owner.incident
) where rn = 1
) i, (
SELECT event_id, COUNT(*) AS number_of_incidents FROM dtm_owner.incident GROUP BY event_id) c
WHERE e.event_id = i.event_id AND e.event_id = c.event_id
);

create or replace view dtm_owner.restore_time (event_id, time_down, time_up) as
(
SELECT * FROM (SELECT event_id, MAX(nvl(time_up, sysdate)) OVER (partition by event_id ORDER BY time_down) as time_down, LEAD(time_down) OVER (partition by event_id ORDER BY time_down) as time_up FROM dtm_owner.incident) WHERE time_down < time_up
union
select a.event_id, b.time_up as time_down, nvl(a.time_up, sysdate) as time_up
from dtm_owner.event a,
(select event_id, max(nvl(time_up, sysdate)) as time_up from dtm_owner.incident group by event_id) b
where a.event_id = b.event_id
and b.time_up != nvl(a.time_up, sysdate)
);

-- Functions
create FUNCTION DTM_OWNER.interval_to_seconds (p_interval INTERVAL DAY TO SECOND) RETURN NUMBER IS
BEGIN
    RETURN EXTRACT(DAY FROM p_interval)*86400 + EXTRACT(HOUR FROM p_interval)*3600 + EXTRACT(MINUTE FROM p_interval)*60 + EXTRACT(SECOND FROM p_interval);
END interval_to_seconds;
/

-- Virtual Columns
alter table dtm_owner.incident add duration_seconds number generated always as (
        ((extract(day from time_up - time_down)) * 86400) +
        ((extract(hour from time_up - time_down)) * 3600) +
        ((extract(minute from time_up - time_down)) * 60) +
        ((extract(second from time_up - time_down)))
    ) virtual;

alter table dtm_owner.incident add reviewed char(1) generated always as ((case when (time_up - time_down) < NUMTODSINTERVAL(30, 'minute') and expert_acknowledged = 'Y' then 'Y' when (time_up - time_down) < NUMTODSINTERVAL(4, 'hour') and root_cause is not null then 'Y' when rar_ext is not null then 'Y' else 'N' end)) virtual;

alter table dtm_owner.incident add review_level varchar2(10) generated always as ((case when (time_up - time_down) < NUMTODSINTERVAL(30, 'minute') then 'ONE' when (time_up - time_down) < NUMTODSINTERVAL(4, 'hour') then 'TWO' else 'THREE_PLUS' end)) virtual;

-- Performance Index
create index incident_perf1 on dtm_owner.incident(event_id);

-- Triggers
create trigger dtm_owner.verify_incident_date_range
    after insert or update
    on dtm_owner.incident
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
        raise_application_error(-20001, 'events overlap (incident check)');
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
/

create trigger dtm_owner.verify_event_date_range
    after insert or update
    on dtm_owner.event
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
        raise_application_error(-20020, 'events overlap (event check)');
    end if;
end;
/
