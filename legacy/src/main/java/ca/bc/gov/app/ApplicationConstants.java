package ca.bc.gov.app;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationConstants {

  public static final String ORACLE_ATTRIBUTE_SCHEMA = "THE";
  public static final String CLIENT_NUMBER = "CLIENT_NUMBER";
  public static final String CLIENT_NUMBER_LITERAL = "clientNumber";
  public static final String MDC_USERID = "X-USER";
  public static final long DEFAULT_PAGE_SIZE = 70L;

}
