package ca.bc.gov.app;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessRequestDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessTypeDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestBodyDto;
import java.util.List;
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
  public static final String SUBMISSION_MAIL_CHANNEL = "submissionMailChannel";
  public static final String SUBMISSION_LEGACY_CLIENT_CHANNEL = "submissionLegacyClientChannel";
  public static final String SUBMISSION_LEGACY_CLIENT_PERSIST_CHANNEL = "submissionLegacyClientPersistChannel";
  public static final String SUBMISSION_LEGACY_LOCATION_CHANNEL = "submissionLegacyLocationChannel";
  public static final String SUBMISSION_LEGACY_CONTACT_CHANNEL = "submissionLegacyContactChannel";
  public static final String SUBMISSION_LEGACY_AGGREGATE_CHANNEL = "submissionLegacyAggregateChannel";
  public static final String SUBMISSION_LEGACY_NOTIFY_CHANNEL = "submissionLegacyNotifyChannel";
  public static final String SUBMISSION_ID = "submission-id";
  public static final String SUBMISSION_STATUS = "submission-status";
  public static final String SUBMISSION_CLIENTID = "submission-clientid";

  public static final String SUBMISSION_TYPE = "submission-type-code";
  public static final String SUBMISSION_NAME = "submission-name";
  public static final String SUBMISSION_MESSAGE_SOURCE = "submissionMessages";
  public static final String PROCESSED_MESSAGE_SOURCE = "processedMessage";
  public static final String CREATED_BY = "createdBy";
  public static final String UPDATED_BY = "updatedBy";
  public static final String FOREST_CLIENT_NUMBER = "forestClientNumber";
  public static final String FOREST_CLIENT_NAME = "forestClientName";
  public static final String INCORPORATION_NUMBER = "incorporationNumber";
  public static final String LOCATION_ID = "locationId";
  public static final String TOTAL = "total";
  public static final String INDEX = "index";
  public static final String PROCESSOR_USER_NAME = "IDIR\\OTTOMATED";
  public static final long ORG_UNIT = 70L;
  public static final String LOCATION_CODE = "locationCode";
  public static final String SUBMISSION_MAIL_BUILD_CHANNEL = "submissionMailBuildChannel";
  public static final String CLIENT_NUMBER = "CLIENT_NUMBER";
  public static final String CLIENT_TYPE_CODE = "CLIENT_TYPE_CODE";
  public static final String SUBMISSION_LEGACY_INDIVIDUAL_CHANNEL = "submissionLegacyIndividualChannel";
  public static final String SUBMISSION_LEGACY_USP_CHANNEL = "submissionLegacyUSPChannel";
  public static final String SUBMISSION_LEGACY_RSP_CHANNEL = "submissionLegacyRSPChannel";
  public static final String SUBMISSION_LEGACY_OTHER_CHANNEL = "submissionLegacyOtherChannel";
  public static final String CLIENT_EXISTS = "client-exists";
  public static final String CLIENT_SUBMITTER_NAME = "client-submitter-name";

  public static final BcRegistryDocumentRequestBodyDto
      BUSINESS_SUMMARY_FILING_HISTORY =
      new BcRegistryDocumentRequestBodyDto(
          new BcRegistryDocumentAccessRequestDto(
              List.of(
                  new BcRegistryDocumentAccessTypeDto("BUSINESS_SUMMARY_FILING_HISTORY")
              )
          )
      );
}
