package ca.bc.gov.app;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationConstant {
  public static final String POSTGRES_ATTRIBUTE_SCHEMA = "nrfc";
  public static final String SUBMISSION_POSTPROCESSOR_CHANNEL = "submissionCompletedChannel";
  public static final String NOTIFICATION_PROCESSING_CHANNEL = "notificationProcessingChannel";
  public static final String SUBMISSION_COMPLETION_CHANNEL = "submissionCompletionChannel";
  public static final String SUBMISSION_LEGACY_CHANNEL = "saveToLegacyChannel";
  public static final String SUBMISSION_LIST_CHANNEL = "submissionListChannel";
  public static final String MATCH_CHECKING_CHANNEL = "matchCheckingChannel";
  public static final String FORWARD_CHANNEL = "forwardChannel";
  public static final String AUTO_APPROVE_CHANNEL = "autoApproveChannel";
  public static final String REVIEW_CHANNEL = "reviewChannel";
  public static final String SUBMISSION_ID = "submission-id";
  public static final String SUBMISSION_STATUS = "submission-status";
  public static final String SUBMISSION_CLIENTID = "submission-clientid";
  public static final String SUBMISSION_NAME = "submission-name";
  public static final String SUBMISSION_MESSAGE_SOURCE = "submissionMessages";
  public static final String PROCESSED_MESSAGE_SOURCE = "processedMessage";
  public static final String SUBMISSION_MAIL_CHANNEL = "submissionMailChannel";

  public static final String SUBMISSION_LEGACY_CLIENT_CHANNEL = "submissionLegacyClientChannel";

  public static final String SUBMISSION_LEGACY_LOCATION_CHANNEL = "submissionLegacyLocationChannel";
  public static final String SUBMISSION_LEGACY_CONTACT_CHANNEL = "submissionLegacyContactChannel";
  public static final String SUBMISSION_LEGACY_AGGREGATE_CHANNEL = "submissionLegacyAggregateChannel";

  public static final String SUBMISSION_LEGACY_NOTIFY_CHANNEL = "submissionLegacyNotifyChannel";


  public static final String CONTACT_QUERY = """
      SELECT
        sc.contact_type_code,
        sc.first_name,
        sc.last_name,
        sc.business_phone_number,
        sc.email_address,
        DENSE_RANK() OVER (ORDER BY slc.submission_location_id) - 1 AS submission_contact_id
      FROM nrfc.submission_contact sc
      LEFT JOIN nrfc.submission_location_contact_xref slc ON slc.submission_contact_id = sc.submission_contact_id
      WHERE sc.submission_id = :submissionId""";
}
