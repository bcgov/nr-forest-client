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
  public static final String AUTO_APPROVE_CHANNEL = "autoApproveChannel";
  public static final String REVIEW_CHANNEL = "reviewChannel";
  public static final String SUBMISSION_ID = "submission-id";
  public static final String SUBMISSION_STATUS = "submission-status";
  public static final String SUBMISSION_CLIENTID = "submission-clientid";

  public static final String SUBMISSION_TYPE = "submission-type-code";
  public static final String SUBMISSION_NAME = "submission-name";

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
  public static final String CLIENT_TYPE_CODE = "CLIENT_TYPE_CODE";
  public static final String CLIENT_SUBMITTER_NAME = "client-submitter-name";

  public static final String MATCH_PARAM_NAME = "corporationName";

  public static final String MATCHING_REASON = "matching-reason";

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
