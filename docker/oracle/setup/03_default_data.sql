alter session set container = XEPDB1;

-- Populate EventType
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(1, 'Accelerator', 1, 'ACC');
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(2, 'Hall A', 2, 'HLA');
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(3, 'Hall B', 3, 'HLB');
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(4, 'Hall C', 4, 'HLC');
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(5, 'Hall D', 5, 'HLD');
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(6, 'LERF', 6, 'LRF');
insert into dtm_owner.event_type (event_type_id, name, weight, abbreviation) values(7, 'Non-Program', 7, 'NP');

-- Populate Workgroup
insert into DTM_OWNER.WORKGROUP (WORKGROUP_ID, NAME) values (1, 'Group 1');
insert into DTM_OWNER.WORKGROUP (WORKGROUP_ID, NAME) values (2, 'Group 2');
insert into DTM_OWNER.WORKGROUP (WORKGROUP_ID, NAME) values (3, 'Group 3');