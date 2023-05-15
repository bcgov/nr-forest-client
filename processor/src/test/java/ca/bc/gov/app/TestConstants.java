package ca.bc.gov.app;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConstants {
  public static final SubmissionDetailEntity SUBMISSION_DETAIL = SubmissionDetailEntity
      .builder()
      .submissionDetailId(1)
      .submissionId(1)
      .incorporationNumber("00000000")
      .organizationName("TEST")
      .businessTypeCode("T")
      .clientTypeCode("T")
      .goodStandingInd("Y")
      .build();

  public static final SubmissionInformationDto SUBMISSION_INFORMATION =
      new SubmissionInformationDto("TEST", "00000000", "Y");
}
