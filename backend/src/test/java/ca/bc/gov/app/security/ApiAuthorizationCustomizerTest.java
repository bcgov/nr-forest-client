package ca.bc.gov.app.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPatternParser;

@DisplayName("Unit Test | ApiAuthorizationCustomizer")
class ApiAuthorizationCustomizerTest {

  private static final PathPatternParser PARSER = new PathPatternParser();

  @Test
  @DisplayName("Staff GET details matcher does not include details-by-id")
  void detailsMatcherDoesNotCaptureDetailsById() {
    var details = PARSER.parse("/api/clients/details/**");
    assertTrue(details.matches(PathContainer.parsePath("/api/clients/details/00123456")));
    assertTrue(details.matches(
        PathContainer.parsePath("/api/clients/details/00123456/related-clients")));
    assertFalse(details.matches(PathContainer.parsePath("/api/clients/details-by-id/BC0772006")));
  }

  @Test
  @DisplayName("Submission list matcher does not include duplicate-check")
  void submissionsListMatcherDoesNotCaptureDuplicateCheck() {
    var list = PARSER.parse("/api/clients/submissions");
    assertTrue(list.matches(PathContainer.parsePath("/api/clients/submissions")));
    assertFalse(list.matches(
        PathContainer.parsePath("/api/clients/submissions/duplicate-check/R/FM00004455")));
  }

  @Test
  @DisplayName("Staff GET client rules are declared before the broad GET grant")
  void staffGetRulesPrecedeBroadClientGetGrant() throws IOException {
    String source = Files.readString(
        Path.of("src/main/java/ca/bc/gov/app/security/ApiAuthorizationCustomizer.java"));
    int detailsGet = source.indexOf("HttpMethod.GET, \"/api/clients/details/**\"");
    int searchGet = source.indexOf("HttpMethod.GET, \"/api/clients/search/**\"");
    int historyGet = source.indexOf("HttpMethod.GET, \"/api/clients/history-logs/**\"");
    int broadGet = source.indexOf("HttpMethod.GET, \"/api/clients/**\"");
    assertTrue(detailsGet > 0 && detailsGet < broadGet);
    assertTrue(searchGet > 0 && searchGet < broadGet);
    assertTrue(historyGet > 0 && historyGet < broadGet);
    assertFalse(source.contains("HttpMethod.GET, \"/api/clients/submissions/**\""));
  }
}
