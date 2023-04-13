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

CREATE SEQUENCE CATEGORY_MONTHLY_GOAL_ID
    INCREMENT BY 1
    START WITH 1
    NOCYCLE
	NOCACHE
	ORDER;

CREATE SEQUENCE EVENT_ID
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
	ORDER

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
    ABBREVIATION         VARCHAR2(3 CHAR) NULL ,
    WEIGHT               INTEGER NOT NULL ,
    CONSTRAINT  EVENT_TYPE_PK PRIMARY KEY (EVENT_TYPE_ID)
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
    CONSTRAINT INCIDENT_PK PRIMARY KEY (INCIDENT_ID),
    CONSTRAINT INCIDENT_FK1 FOREIGN KEY (EVENT_ID) REFERENCES DTM_OWNER.EVENT (EVENT_ID) ON DELETE CASCADE
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
    REVIEWED_BY          INTEGER NULL ,
    ROOT_CAUSE           VARCHAR2(512 CHAR) NULL ,
    RAR_ID               INTEGER NULL ,
    EXPERT_ACKNOWLEDGED  CHAR(1) DEFAULT 'N' NOT NULL CONSTRAINT INCIDENT_AUD_CK1 CHECK (EXPERT_ACKNOWLEDGED IN ('N', 'Y', 'R')),
    RAR_EXT              VARCHAR(12 CHAR) NULL ,
	RAR_UPLOADED_DATE    TIMESTAMP(0) WITH LOCAL TIME ZONE NULL ,
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

CREATE TABLE DTM_OWNER.SYSTEM
(
    SYSTEM_ID   NUMBER NOT NULL CONSTRAINT SYSTEM_PK PRIMARY KEY,
    NAME        VARCHAR2(128 CHAR) NOT NULL CONSTRAINT SYSTEM_AK1 UNIQUE,
    CATEGORY_ID NUMBER NOT NULL CONSTRAINT SYSTEM_FK1 REFERENCES DTM_OWNER.CATEGORY ON DELETE SET NULL,
    WEIGHT      NUMBER
);

CREATE TABLE DTM_OWNER.REGION
(
    REGION_ID NUMBER NOT NULL CONSTRAINT REGION_PK PRIMARY KEY,
    NAME      VARCHAR2(128 CHAR) NOT NULL,
    ALIAS     VARCHAR2(128 CHAR),
    WEIGHT    NUMBER
);

CREATE TABLE DTM_OWNER.COMPONENT
(
    COMPONENT_ID         NUMBER NOT NULL CONSTRAINT COMPONENT_PK PRIMARY KEY,
    NAME                 VARCHAR2(128 char) NOT NULL CONSTRAINT COMPONENT_CK4 CHECK (INSTR(NAME, '*') = 0),
    SYSTEM_ID            NUMBER NOT NULL CONSTRAINT COMPONENT_FK2 REFERENCES DTM_OWNER.SYSTEM ON DELETE SET NULL,
    DATA_SOURCE          VARCHAR2(24 CHAR) DEFAULT 'INTERNAL' NOT NULL CONSTRAINT COMPONENT_CK1 CHECK (DATA_SOURCE IN ('INTERNAL', 'CED', 'LED', 'UED')),
    DATA_SOURCE_ID       NUMBER,
    REGION_ID            NUMBER NOT NULL CONSTRAINT COMPONENT_FK1 REFERENCES DTM_OWNER.REGION,
    WEIGHT               NUMBER,
    MASKED               CHAR(1 CHAR) DEFAULT 'N' NOT NULL CONSTRAINT COMPONENT_CK3 CHECK (MASKED IN ('Y', 'N')),
    MASKED_COMMENT       VARCHAR2(512 CHAR),
    MASKED_DATE          DATE,
    MASKED_BY            NUMBER,
    ADDED_DATE           DATE DEFAULT SYSDATE NOT NULL,
    UNPOWERED_YN         CHAR(1 CHAR) DEFAULT 'N' NOT NULL CONSTRAINT COMPONENT_CK5 CHECK (UNPOWERED_YN IN ('Y', 'N')),
    MASK_EXPIRATION_DATE DATE,
    MASK_TYPE_ID         NUMBER CONSTRAINT COMPONENT_CK6 CHECK (MASK_TYPE_ID IN (150, 200, 250)),
    NAME_ALIAS           VARCHAR2(128 CHAR),
    CONSTRAINT COMPONENT_AK1 UNIQUE (NAME, SYSTEM_ID),
    CONSTRAINT COMPONENT_AK2 UNIQUE (SYSTEM_ID, COMPONENT_ID),
    CONSTRAINT COMPONENT_TABLE_CK1 CHECK ((DATA_SOURCE_ID IS NOT NULL AND DATA_SOURCE = 'CED') OR
               (DATA_SOURCE_ID IS NOT NULL AND DATA_SOURCE = 'LED') OR
               (DATA_SOURCE_ID IS NOT NULL AND DATA_SOURCE = 'UED') OR
               (DATA_SOURCE_ID IS NULL AND DATA_SOURCE = 'INTERNAL'))
);

CREATE TABLE DTM_OWNER.WORKGROUP
(
    WORKGROUP_ID     NUMBER NOT NULL CONSTRAINT WORKGROUP_PK PRIMARY KEY,
    NAME             VARCHAR2(128 CHAR) NOT NULL CONSTRAINT WORKGROUP_AK1 UNIQUE
);

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

CREATE OR REPLACE VIEW DTM_OWNER.SYSTEM_ALPHA_CATEGORY AS
(
SELECT SYSTEM_ID, CATEGORY_ID from DTM_OWNER.SYSTEM
);

CREATE OR REPLACE VIEW DTM_OWNER.SYSTEM_ALPHA_CATEGORY_PLUS AS
(
SELECT SYSTEM_ID, CATEGORY_ID from DTM_OWNER.SYSTEM
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