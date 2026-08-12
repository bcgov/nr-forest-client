CREATE TABLE THE.SCALE_SITE_CONTACT (
    SCALE_SITE_ID_NMBR           VARCHAR2(4 BYTE)   NOT NULL,
    CLIENT_CONTACT_ID            NUMBER(12, 0)      NOT NULL,
    CONTACT_ROLE_DESCRIPTION     VARCHAR2(40 BYTE)  NOT NULL,
    PRIMARY_CONTACT_IND          VARCHAR2(1 BYTE)   NOT NULL
        CONSTRAINT AVCON_1198107382_PRIMA_000
        CHECK (PRIMARY_CONTACT_IND IN ('N', 'Y')),
    SITE_INFORMATION_ACCESS_IND  VARCHAR2(1 BYTE)   NOT NULL
        CONSTRAINT AVCON_1198107382_SITE__000
        CHECK (SITE_INFORMATION_ACCESS_IND IN ('N', 'Y')),
    EFFECTIVE_DATE               DATE               NOT NULL,
    EXPIRY_DATE                  DATE,
    ENTRY_TIMESTAMP              DATE               NOT NULL,
    ENTRY_USERID                 VARCHAR2(30 BYTE)  NOT NULL,
    UPDATE_TIMESTAMP             DATE               NOT NULL,
    UPDATE_USERID                VARCHAR2(30 BYTE)  NOT NULL,
    REVISION_COUNT               NUMBER(15)         NOT NULL
)
NO INMEMORY;

COMMENT ON TABLE THE.SCALE_SITE_CONTACT IS
    'A SITE CONTACT is an individual the ministry may contact at or in regards to a SCALE SITE. Note: This does not include SCALERS.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.SCALE_SITE_ID_NMBR IS
    'System generated number to uniquely identify a SCALE SITE. Legacy SCALE SITE IDs where composed of both alpha and numerical characters. All new SCALE SITE IDs will be generated sequentially starting from 1111. Note: This name has been retained from old SCS for HBS compatibility.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.CLIENT_CONTACT_ID IS
    'Reference from CLIENT';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.CONTACT_ROLE_DESCRIPTION IS
    'Description of the contact"s role at the SCALE SITE. For example: - Bucking Contractor - Site Contact';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.PRIMARY_CONTACT_IND IS
    'Indicates whether or not the contact is considered the primary contact.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.SITE_INFORMATION_ACCESS_IND IS
    'Indicates whether or not the SITE CONTACT has been granted the ability to view the site"s information via the SCS application system. The site contact must have: 1. Authentication via WebADE 2. Authorization to access specific SCS web services via ADAM';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.EFFECTIVE_DATE IS
    'Date the record became effective.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.EXPIRY_DATE IS
    'Date the record expired.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.ENTRY_TIMESTAMP IS
    'The date and time the record was initially created.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.ENTRY_USERID IS
    'The unique user id who initially created the record.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.UPDATE_TIMESTAMP IS
    'The date and time on which the record was last updated.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.UPDATE_USERID IS
    'The unique user id who last updated the record.';

COMMENT ON COLUMN THE.SCALE_SITE_CONTACT.REVISION_COUNT IS
    'The number of times an entry has been updated, starting with 1 when it was first created.';

ALTER TABLE THE.SCALE_SITE_CONTACT
    ADD CONSTRAINT SICT_CC_FK
    FOREIGN KEY (CLIENT_CONTACT_ID)
    REFERENCES THE.CLIENT_CONTACT (CLIENT_CONTACT_ID)
    NOT DEFERRABLE;
