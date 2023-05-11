package ca.bc.gov.app;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessRequestDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessTypeDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestBodyDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains constants used throughout the application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationConstant {
  public static final String POSTGRES_ATTRIBUTE_SCHEMA = "nrfc";

  public static final String USERID_HEADER = "x-user-id";
  public static final String USERMAIL_HEADER = "x-user-email";
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
