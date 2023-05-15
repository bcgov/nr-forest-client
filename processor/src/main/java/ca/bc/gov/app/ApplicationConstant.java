package ca.bc.gov.app;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationConstant {
  public static final String POSTGRES_ATTRIBUTE_SCHEMA = "nrfc";
  public static final String SUBMISSION_LIST_CHANNEL = "submissionListChannel";
  public static final String MATCH_CHECKING_CHANNEL = "matchCheckingChannel";
  public static final String FORWARD_CHANNEL = "forwardChannel";
  public static final String AUTO_APPROVE_CHANNEL = "autoApproveChannel";
  public static final String REVIEW_CHANNEL = "reviewChannel";
  public static final String SUBMISSION_ID = "submission-id";
}
