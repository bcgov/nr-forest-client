--
-- ER/Studio Data Architect SQL Code Generation
-- Project :      FSA_OldGrowth.DM1
-- Author :       Maria Martinez
--
-- Date Created : Friday, May 06, 2022 16:31:29
-- Target DBMS : PostgreSQL 9.x
--

drop table if exists deferral_category_code;

-- 
-- TABLE: deferral_category_code
--

create table deferral_category_code (
    deferral_category_code  varchar(5)      not null,
    description             varchar(100)    not null,
    effective_date          date            not null,
    expiry_date             date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp        timestamp       default current_timestamp not null,
    update_timestamp        timestamp       default current_timestamp,
    create_user             varchar(60)     not null,
    update_user             varchar(60)
)
;


comment on column deferral_category_code.deferral_category_code is 'Deferral category selects by the submitter. Multiple deferral categories are possible.'
;
comment on column deferral_category_code.description is 'The display quality description of the code value.'
;
comment on column deferral_category_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.'
;
comment on column deferral_category_code.expiry_date is 'The date on which the code value has expired or is expected to expire. Default 9999-12-31'
;
comment on column deferral_category_code.create_timestamp is 'The date and time the record was created.'
;
comment on column deferral_category_code.update_timestamp is 'The date and time the record was created or last updated.'
;
comment on column deferral_category_code.create_user is 'The user or proxy account that created the record.'
;
comment on column deferral_category_code.update_user is 'The user or proxy account that created or last updated the record.'
;
comment on table deferral_category_code is 'Deferral category selects by the submitter. Multiple deferral categories are possible.'
;


alter table deferral_category_code add 
    constraint deferral_category_pk primary key (deferral_category_code)
;


--
-- Insert statements to code tables
---

INSERT INTO DEFERRAL_CATEGORY_CODE (DEFERRAL_CATEGORY_CODE, DESCRIPTION, EFFECTIVE_DATE, CREATE_USER) VALUES ('BT', 'Big treed', CURRENT_TIMESTAMP, 'mariamar');
INSERT INTO DEFERRAL_CATEGORY_CODE (DEFERRAL_CATEGORY_CODE, DESCRIPTION, EFFECTIVE_DATE, CREATE_USER) VALUES ('AT', 'Ancient', CURRENT_TIMESTAMP, 'mariamar');
INSERT INTO DEFERRAL_CATEGORY_CODE (DEFERRAL_CATEGORY_CODE, DESCRIPTION, EFFECTIVE_DATE, CREATE_USER) VALUES ('RT', 'Remnant', CURRENT_TIMESTAMP, 'mariamar');


-- -- 
-- -- TABLE: spatial_file
-- --
-- CREATE EXTENSION postgis;

-- drop table if exists spatial_file;

-- CREATE TABLE spatial_file (
--     spatial_file_id   serial          not null,
--     file_name         varchar(60)    not null,
--     geometry_type     varchar(20)    not null,
--     geom              geometry
-- )
-- ;

-- comment on column spatial_file.spatial_file_id is 'A sequential id assigned to an spatial geometry file.'
-- ;
-- comment on column spatial_file.file_name is 'The name of the geometry file.'
-- ;
-- comment on column spatial_file.geometry_type is 'The geometry type.'
-- ;
-- comment on column spatial_file.geom is 'The geometry file data.'
-- ;
-- comment on table spatial_file is 'Spatial file (accepted formats: GEOJSON) (Use the naming convention: Spatialfile_ForestFileID_CP)'
-- ;

-- alter table spatial_file add 
--     constraint spatial_file_id primary key (spatial_file_id)
-- ;


-- INSERT INTO spatial_file (file_name, geometry_type, geom)
-- VALUES (
--     'polygon_file',
-- 	'Polygon',
--     ST_GeomFromGeoJSON(
--         '{
--             "type": "Polygon",
--             "coordinates": [ [ 
-- 			  [1474614.5923999995, 555392.24159999937],
-- 			  [1474537.8630999997, 555275.82469999976],
-- 			  [1474588.1340999994, 555146.17860000022],
-- 			  [1474723.0717999991, 555080.0326000005],
-- 			  [1474818.3220000006, 555138.24110000022],
-- 			  [1474902.9889000002, 555220.26209999993],
-- 			  [1474818.3220000006, 555334.03309999965],
-- 			  [1474701.9050999992, 555437.22079999931],
-- 			  [1474614.5923999995, 555392.24159999937]
-- 			] ]
--         }'
--     )
-- )
