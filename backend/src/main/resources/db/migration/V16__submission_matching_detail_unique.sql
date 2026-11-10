-- Deduplicate existing submission_matching_detail rows keeping the processed or most recent entry
WITH ranked_details AS (
    SELECT
        submission_matching_detail_id,
        ROW_NUMBER() OVER (
            PARTITION BY submission_id
            ORDER BY
                submission_matching_processed DESC,
                submission_matching_processed_time DESC NULLS LAST,
                confirmed_match_timestamp DESC NULLS LAST,
                submission_matching_detail_id DESC
        ) AS rn
    FROM nrfc.submission_matching_detail
)
DELETE FROM nrfc.submission_matching_detail
WHERE submission_matching_detail_id IN (
    SELECT submission_matching_detail_id
    FROM ranked_details
    WHERE rn > 1
);

ALTER TABLE nrfc.submission_matching_detail
ADD CONSTRAINT submission_matching_detail_submission_id_uk UNIQUE (submission_id);
