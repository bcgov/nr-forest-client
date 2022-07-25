-- 
-- TABLE: client_public
--

create table client_public (
    client_number       varchar(60)       not null,
    client_name         varchar(60),
    legal_first_name    varchar(60),
    legal_middle_name   varchar(60),
    client_status_code  varchar(60),
    client_type_code    varchar(60)
)
;

alter table client_public add 
    constraint client_number primary key (client_number)
;
