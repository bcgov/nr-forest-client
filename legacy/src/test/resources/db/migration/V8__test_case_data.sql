-- The below examples are for testing purposes only. They are in a state that makes sense and can be seen in the green interface (allegedly)

-- Example 1
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000138', 'C', 'JERROM', null, null, 'ACT', null, 'BC', '4043442', null, null, null, null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\ejerrom0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\jjerrom0', 70, 3);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000138', '00', 'MR', null, '00461 Huxley Center', null, null, 'CARLETON PLACE', 'ON', 'K7CK7C', 'CANADA', '4608470812', null, null, null, 'egirault0@zdnet.com', 'N', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\HPHAUP0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\BDOLE0', 70, 2);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000138', '00', 'AP', 'ELLISSA MCILVORAY', '7574522379', '2605980052', '2267331172', 'EMCILVORAY2@CNN.COM', CURRENT_TIMESTAMP(6), 'IDIR\\EMCILVORAY2', 70, CURRENT_TIMESTAMP(6), 'IDIR\\EMCILVORAY2', 70, 1);

-- Example 2
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000171', 'I', 'HAUCK', 'RICARDO', 'NATKA', 'ACT', DATE '1973-09-08', null, null, null, 'ABDL', '3709477', null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\NHAUCK0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\NHAUCK0', 70, 2);
insert into THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID, CLIENT_NUMBER, DOING_BUSINESS_AS_NAME, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_dba_seq.NEXTVAL, '00000171', 'BLOGSPAN', CURRENT_TIMESTAMP(6), 'IDIR\\WJILLINGS0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\OMARLING0', 70, 4);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000171', '00', 'RIVERBEND ESTATES ADDRESS', null, null, '0 ESCH COURT', 'PO BOX 662', 'SILVER SPRING', 'MD', 'UNITED STATES', '20910', '2409337435', null, '9877835418', null, 'BCULLRFORD0@BOSTON.COM', 'Y', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\EBULGER0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\OLEBARREE0', 70, 920);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000171', '00', 'LB', 'REED URRIDGE', '8715340311', '1137347168', '4684391266', 'RURRIDGE1@DEVIANTART.COM', CURRENT_TIMESTAMP(6), 'IDIR\\RURRIDGE1', 70, CURRENT_TIMESTAMP(6), 'IDIR\\RURRIDGE1', 70, 3);

-- Example 3
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000103', 'I', 'FISHBOURNE', 'ADELICE', 'LANITA', 'ACT', DATE '1966-02-04', 'FM', '3176776', null, 'BCRE', 'FM3176776', null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\LFISHBOURNE0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\LFISHBOURNE0', 70, 4);
insert into THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID, CLIENT_NUMBER, DOING_BUSINESS_AS_NAME, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_dba_seq.NEXTVAL, '00000103', 'TWIMBO', CURRENT_TIMESTAMP(6), 'IDIR\\DMCSWEENEY0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\LLANSDOWN0', 70, 5);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000103', '00', 'MEADOWBROOK HILLS ADDRESS', null, '83434 CROWNHARDT TRAIL', 'IN CARE OF EWAN', null, 'WILLOWDALE', 'ON', 'CANADA', 'M3H5V5', '7479368604', '3468588989', '6187239694', null, 'MSELLIMAN0@YCOMBINATOR.COM', 'N', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\NSAMMON0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\BGRIMSDALE0', 70, 621);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000103', '00', 'GP', 'UDALL TURFES', '9999797279', '2894837433', '1056960533', 'UTURFES0@CNN.COM', CURRENT_TIMESTAMP(6), 'IDIR\\UTURFES0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\UTURFES0', 70, 4);

-- Example 4
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000117', 'C', 'SHANAHAN LLC', null, null, 'ACT', null, 'BC', '2007883', null, null, null, null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\LSHANAHANLLC1', 70, CURRENT_TIMESTAMP(6), 'IDIR\\KSHANAHANLLC1', 70, 4);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000117', '00', 'RIVERBEND ESTATES ADDRESS', null, null, '21198 HIGH CROSSING DRIVE', null, 'LANSING', 'MI', 'UNITED STATES', '48919', '5174798837', '4026307553', '7019636995', null, 'KDILLOW1@BARNESANDNOBLE.COM', 'N', null, 'N', CURRENT_TIMESTAMP(6), 'IDIR\\BBORTHE1', 70, CURRENT_TIMESTAMP(6), 'IDIR\\HMEDLAND1', 70, 619);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000117', '01', 'MAPLEWOOD HEIGHTS ADDRESS', null, null, '7942 ELIOT TRAIL', null, 'VAN NUYS', 'CA', 'UNITED STATES', '91499', null, null, null, null, 'JTEMBLETT2@NEWYORKER.COM', 'N', null, 'N', CURRENT_TIMESTAMP(6), 'IDIR\\RBELCH2', 70, CURRENT_TIMESTAMP(6), 'IDIR\\MTREMBERTH2', 70, 940);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000117', '00', 'TC', 'EBENESER STOWER', '2889133175', '5921950826', '1341757231', 'ESTOWER3@ALTERVISTA.ORG', CURRENT_TIMESTAMP(6), 'IDIR\\ESTOWER3', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ESTOWER3', 70, 7);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000117', '00', 'SI', 'ANDY EDLAND', '9589011336', '4893348133', '6569230088', 'AEDLAND4@AOL.COM', CURRENT_TIMESTAMP(6), 'IDIR\\AEDLAND4', 70, CURRENT_TIMESTAMP(6), 'IDIR\\AEDLAND4', 70, 6);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000117', '01', 'SP', 'ILSE CAROLL', '8966265253', '7276718715', '8475116099', 'ICAROLL5@LIVEINTERNET.RU', CURRENT_TIMESTAMP(6), 'IDIR\\ICAROLL5', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ICAROLL5', 70, 1);

-- Example 5
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000144', 'C', 'STARK AND SONS', null, null, 'ACT', null, 'BC', '2504984', null, null, null, null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\KSTARKANDSONS2', 70, CURRENT_TIMESTAMP(6), 'IDIR\\OSTARKANDSONS2', 70, 1);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000144', '00', 'WILLOW CREEK ADDRESS', null, null, '1 LIEN CIRCLE', null, 'MARATHON', 'ON', 'CANADA', 'P6A7O2', '3278750427', '4721392395', null, null, 'SCICCO3@CDC.GOV', 'Y', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\RSCOGGIN3', 70, CURRENT_TIMESTAMP(6), 'IDIR\\HKORDOVA3', 70, 168);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000144', '00', 'CL', 'MORTEN FOTITT', '2606539520', '2146135966', '1069077712', 'MFOTITT6@SMH.COM.AU', CURRENT_TIMESTAMP(6), 'IDIR\\MFOTITT6', 70, CURRENT_TIMESTAMP(6), 'IDIR\\MFOTITT6', 70, 1);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000144', '00', 'AP', 'AVIVA CLEARIE', '5825288480', '1117397076', '6867167959', 'ACLEARIE7@CHICAGOTRIBUNE.COM', CURRENT_TIMESTAMP(6), 'IDIR\\ACLEARIE7', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ACLEARIE7', 70, 6);

-- Example 6
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000101', 'C', 'KLEIN GROUP', null, null, 'ACT', null, 'BC', '8242386', null, null, null, null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\PKLEINGROUP3', 70, CURRENT_TIMESTAMP(6), 'IDIR\\VKLEINGROUP3', 70, 1);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000101', '00', 'RIVERBEND ESTATES ADDRESS', null, '1 CHEROKEE PLAZA', 'IN CARE OF GEOFFRY', null, 'COLUMBUS', 'GA', 'UNITED STATES', '31998', '7069657172', null, '2478830406', null, 'MTIES4@YELLOWPAGES.COM', 'Y', null, 'N', CURRENT_TIMESTAMP(6), 'IDIR\\JBURNAPP4', 70, CURRENT_TIMESTAMP(6), 'IDIR\\DLICHTFOTH4', 70, 802);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000101', '00', 'SS', 'AVERY MCCALLISTER', '9688296499', '2218198891', '2047810215', 'AMCCALLISTER8@PHP.NET', CURRENT_TIMESTAMP(6), 'IDIR\\AMCCALLISTER8', 70, CURRENT_TIMESTAMP(6), 'IDIR\\AMCCALLISTER8', 70, 6);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000101', '00', 'IL', 'LU YEZAFOVICH', '3144491415', '3664518621', '2549519319', 'LYEZAFOVICH9@GMPG.ORG', CURRENT_TIMESTAMP(6), 'IDIR\\LYEZAFOVICH9', 70, CURRENT_TIMESTAMP(6), 'IDIR\\LYEZAFOVICH9', 70, 1);

-- Example 7
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000145', 'I', 'BLIND', 'DARBIE', null, 'ACT', DATE '1967-10-30', 'FM', '4911611', null, 'BCRE', 'FM4911611', null, null, 'Aenean lectus. Pellentesque eget nunc. Donec quis orci eget orci vehicula condimentum.', CURRENT_TIMESTAMP(6), 'IDIR\\CBLIND4', 70, CURRENT_TIMESTAMP(6), 'IDIR\\MBLIND4', 70, 4);
insert into THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID, CLIENT_NUMBER, DOING_BUSINESS_AS_NAME, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_dba_seq.NEXTVAL, '00000145', 'MINYX', CURRENT_TIMESTAMP(6), 'IDIR\\LGASSON1', 70, CURRENT_TIMESTAMP(6), 'IDIR\\FSEMARKE1', 70, 1);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000145', '00', 'MAPLEWOOD HEIGHTS ADDRESS', null, null, '71993 THOMPSON JUNCTION', null, 'SAINT LOUIS', 'MO', 'UNITED STATES', '63131', '6362925975', '8225299384', '8773834294', null, 'ULAUDER5@SHINYSTAT.COM', 'N', null, 'N', CURRENT_TIMESTAMP(6), 'IDIR\\LFANNER5', 70, CURRENT_TIMESTAMP(6), 'IDIR\\HLANGFORD5', 70, 985);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000145', '00', 'CL', 'SHER SIMIONATO', '6221381840', '7597166609', '4768680390', 'SSIMIONATO0@EBAY.COM', CURRENT_TIMESTAMP(6), 'IDIR\\SSIMIONATO0', 70, CURRENT_TIMESTAMP(6), 'IDIR\\SSIMIONATO0', 70, 6);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000145', '00', 'CL', 'ALANO CLUDERAY', '2954008825', '9723971099', '3567458718', 'ACLUDERAY1@MOZILLA.COM', CURRENT_TIMESTAMP(6), 'IDIR\\ACLUDERAY1', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ACLUDERAY1', 70, 4);

-- Example 8
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000114', 'C', 'POLLICH-ABERNATHY', null, null, 'ACT', null, 'BC', '4712252', null, null, null, null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\TPOLLICHABERNATHY5', 70, CURRENT_TIMESTAMP(6), 'IDIR\\SPOLLICHABERNATHY5', 70, 4);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000114', '00', 'RIVERBEND ESTATES ADDRESS', null, null, '6584 STUART JUNCTION', null, 'MORINVILLE', 'AB', 'CANADA', 'T8R4U4', '3837672187', '9192114358', null, null, 'SGERCKENS6@DELL.COM', 'Y', null, 'N', CURRENT_TIMESTAMP(6), 'IDIR\\RMONTFORD6', 70, CURRENT_TIMESTAMP(6), 'IDIR\\PCROSSON6', 70, 217);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000114', '01', 'BIRCHWOOD PARK ADDRESS', null, null, '510 FULTON PLAZA', null, 'FORT NELSON', 'BC', 'CANADA', 'T9J9R1', '8823899685', '2911911672', '5229472760', null, 'RGRIMSTON9@DMOZ.ORG', 'Y', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\IBEWICKE9', 70, CURRENT_TIMESTAMP(6), 'IDIR\\CSIDDEN9', 70, 617);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000114', '00', 'RC', 'WIT O''HARA', '7958564554', '2736384782', '6168346506', 'WOHARA2@SOUP.IO', CURRENT_TIMESTAMP(6), 'IDIR\\WOHARA2', 70, CURRENT_TIMESTAMP(6), 'IDIR\\WOHARA2', 70, 1);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000114', '00', 'AR', 'ANGELO GLANDER', '3681986668', '8313586603', '2884687489', 'AGLANDER3@STORIFY.COM', CURRENT_TIMESTAMP(6), 'IDIR\\AGLANDER3', 70, CURRENT_TIMESTAMP(6), 'IDIR\\AGLANDER3', 70, 6);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000114', '00', 'RC', 'PATTI LEVEY', '4063553713', '4542900616', '2847831479', 'PLEVEY4@UN.ORG', CURRENT_TIMESTAMP(6), 'IDIR\\PLEVEY4', 70, CURRENT_TIMESTAMP(6), 'IDIR\\PLEVEY4', 70, 7);

-- Example 9
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000137', 'I', 'LINDHE', 'MATELDA', null, 'ACT', DATE '1960-10-09', 'FM', '2248989', null, 'BCRE', 'FM2248989', null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\RLINDHE6', 70, CURRENT_TIMESTAMP(6), 'IDIR\\LLINDHE6', 70, 7);
insert into THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID, CLIENT_NUMBER, DOING_BUSINESS_AS_NAME, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_dba_seq.NEXTVAL, '00000137', 'JABBERTYPE', CURRENT_TIMESTAMP(6), 'IDIR\\KJANUARYST2', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ACRAIGS2', 70, 7);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000137', '00', 'MAPLEWOOD HEIGHTS ADDRESS', null, '5452 DARWIN PLAZA', 'IN CARE OF CORNELLE', null, 'NAPERVILLE', 'IL', 'UNITED STATES', '60567', '6304091986', null, '3903629484', null, 'ZSABATE7@FOXNEWS.COM', 'Y', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\TALVARADO7', 70, CURRENT_TIMESTAMP(6), 'IDIR\\SDRISSELL7', 70, 953);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000137', '00', 'BM', 'RICARDO BRISLEN', '8225477595', '7589636074', '8598150782', 'RBRISLEN5@UN.ORG', CURRENT_TIMESTAMP(6), 'IDIR\\RBRISLEN5', 70, CURRENT_TIMESTAMP(6), 'IDIR\\RBRISLEN5', 70, 3);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000137', '00', 'AP', 'ANNABAL LENTON', '4478270485', '5499181737', '2749760844', 'ALENTON6@TRELLIAN.COM', CURRENT_TIMESTAMP(6), 'IDIR\\ALENTON6', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ALENTON6', 70, 2);

-- Example 10
insert into THE.FOREST_CLIENT (CLIENT_NUMBER, CLIENT_TYPE_CODE, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, BIRTHDATE, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
values ('00000123', 'C', 'REICHERT, KILBACK AND EMARD', null, null, 'ACT', null, 'BC', '7664664', null, null, null, null, null, null, CURRENT_TIMESTAMP(6), 'IDIR\\AREICHERTKILBACKANDEMARD', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ZREICHERTKILBACKANDEMARD', 70, 4);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000123', '00', 'SILVER LAKE ADDRESS', null, '520 BUTTERFIELD AVENUE', 'IN CARE OF EMMIE', null, 'DAYTON', 'OH', 'UNITED STATES', '45432', '9377965465', null, '5308577668', null, 'DWASMUTH8@HUD.GOV', 'N', null, 'N', CURRENT_TIMESTAMP(6), 'IDIR\\ETEML8', 70, CURRENT_TIMESTAMP(6), 'IDIR\\SDICTUS8', 70, 490);
insert into THE.CLIENT_LOCATION (CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_2, ADDRESS_1, ADDRESS_3, CITY, PROVINCE, COUNTRY, POSTAL_CODE, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values ('00000123', '01', 'OAKWOOD PARK ADDRESS', null, null, '510 FULTON PLAZA', null, 'FORT MCMURRAY', 'AB', 'CANADA', 'T9J9R1', '8823899685', '2911911672', '5229472760', null, 'RGRIMSTON9@DMOZ.ORG', 'Y', null, 'Y', CURRENT_TIMESTAMP(6), 'IDIR\\IBEWICKE9', 70, CURRENT_TIMESTAMP(6), 'IDIR\\CSIDDEN9', 70, 617);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000123', '01', 'BN', 'MARGERY INGLESFIELD', '3588888017', '7869238751', '4877523072', 'MINGLESFIELD7@LAST.FM', CURRENT_TIMESTAMP(6), 'IDIR\\MINGLESFIELD7', 70, CURRENT_TIMESTAMP(6), 'IDIR\\MINGLESFIELD7', 70, 1);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000123', '00', 'EX', 'JUNINA MATOKHNIN', '2833254982', '1416006903', '9751196928', 'JMATOKHNIN8@WP.COM', CURRENT_TIMESTAMP(6), 'IDIR\\JMATOKHNIN8', 70, CURRENT_TIMESTAMP(6), 'IDIR\\JMATOKHNIN8', 70, 5);
insert into THE.CLIENT_CONTACT (CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
values (client_contact_seq.NEXTVAL, '00000123', '01', 'AR', 'SAUNCHO FINGLETON', '7674316947', '1791392515', '8891201757', 'SFINGLETON9@ARCHIVE.ORG', CURRENT_TIMESTAMP(6), 'IDIR\\SFINGLETON9', 70, CURRENT_TIMESTAMP(6), 'IDIR\\SFINGLETON9', 70, 6);

-- Example 11
INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000157', 'UNEXPECTED NATION', NULL, NULL, 'ACT', 'B', NULL, NULL, NULL, 'DINA', '0', NULL, NULL, NULL, 'ADDTIONAL ADDRESSES TO THIS CLIENT', CURRENT_TIMESTAMP(6), 'CONV', 70, CURRENT_TIMESTAMP(6), 'IDIR\\EAPAUL', 70, 4);
INSERT INTO THE.CLIENT_LOCATION
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000157', '00', NULL, '1', 'BOX 86131', NULL, NULL, 'NORTH VANCOUVER', 'BC', 'V7L4J5', 'CANADA', '6049999999', NULL, NULL, '6049999998', NULL, 'N', NULL, 'N', NULL, CURRENT_TIMESTAMP(6), 'ITSWATTLES', 70, CURRENT_TIMESTAMP(6), 'CONV', 70, 1);
INSERT INTO THE.CLIENT_LOCATION
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000157', '01', 'THAT ADDRESS', '40000', 'ATTN: JOHNATHAN WICK', 'BOX 42999', NULL, 'SQUAMISH', 'BC', 'V0N3G0', 'CANADA', NULL, NULL, NULL, NULL, NULL, 'N', NULL, 'N', NULL, CURRENT_TIMESTAMP(6), 'MILESM', 70, CURRENT_TIMESTAMP(6), 'MILESM', 70, 1);
INSERT INTO THE.CLIENT_LOCATION
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000157', '03', '04', ' ', 'SEE "02" LOCATION', NULL, NULL, 'SQUAMISH', 'BC', 'A1A2B2', 'CANADA', NULL, NULL, NULL, NULL, NULL, 'Y', NULL, 'Y', NULL, CURRENT_TIMESTAMP(6), 'IDIR\\TOOMUCH', 70, CURRENT_TIMESTAMP(6), 'IDIR\\ALFRED', 70, 1);
INSERT INTO THE.CLIENT_LOCATION
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000157', '02', 'MANAGING AGENT FOR W1L97', ' ', 'C/O THIS FORESTRY LP', '1124 ENTERPRISE WAY', 'ATTN:  SCOTTY', 'SQUAMISH', 'BC', 'V8B0E9', 'CANADA', '6049998888', NULL, '6048899999', NULL, 'JEFF.JEFFERSON@MAMAMIA.CA', 'N', NULL, 'N', 'AS PER 2012-02-08 EMAIL FROM JEFF JEFFERSON - ATTACHED AUTHORIZATION LETTER SIGNED BY MASTER CHIEF OF THE UNITED NATIONS SPACE COMMAND ASSIGNING THIS FORESTRY LP AS THE AGENT FOR W1L97.  SEE ABC TRIM FILE 12345-67/W1L97.', CURRENT_TIMESTAMP(6), 'IDIR\\JOHN117', 70, CURRENT_TIMESTAMP(6), 'IDIR\\CORTANA', 70, 3);
INSERT INTO THE.CLIENT_CONTACT
(CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES(client_contact_seq.NEXTVAL, '00000157', '00', 'TN', 'JACK BEANSTALK', NULL, NULL, NULL, 'RYAN.BEANSTALK@FABLES.CA', CURRENT_TIMESTAMP(6), 'IDIR\\JRYAN', 70, CURRENT_TIMESTAMP(6), 'IDIR\\JGREER', 70, 2);

-- Insert the initial client number counter
UPDATE THE.MAX_CLIENT_NMBR SET client_number = (SELECT LPAD(TO_NUMBER(NVL(max(CLIENT_NUMBER),'0'))+1,8,'0') FROM FOREST_CLIENT);
