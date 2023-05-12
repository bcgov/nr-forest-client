-- For test only
INSERT INTO nrfc.submission
(submission_id, submission_status_code, submission_type_code, submission_date, update_timestamp, create_user, update_user)
VALUES(1, 'N', 'SPP', '2023-05-02 00:00:00.000', '2023-05-04 10:52:01.689', '58d50cde-c77c-4f4d-8de3-34c4d9367a68', '20d9aa0c-7c62-4fd2-a725-d950ed728d36');

INSERT INTO nrfc.submission_detail
(submission_detail_id, submission_id, business_type_code, incorporation_number, organization_name, client_type_code, good_standing_ind)
VALUES(1, 1, 'R', 'GP00000004', 'GREYJOY SEAPORTS INC.', 'I', NULL);

