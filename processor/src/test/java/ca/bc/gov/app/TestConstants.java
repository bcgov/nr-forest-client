package ca.bc.gov.app;

import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import java.util.Map;
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

  public static final EmailRequestDto EMAIL_REQUEST = new EmailRequestDto(
      "ABC1234",
      "Test Corp",
      "testuserid",
      "Test User",
      "testuser@mail.tst",
      "test",
      "Processor Tests",
      Map.of()
  );
}
