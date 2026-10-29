package ca.bc.gov.app.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Unit Test | WiremockLogNotifier")
class WiremockLogNotifierTest {

  private static final String DUMMY_BASIC_AUTH_TOKEN =
      Base64.getEncoder().encodeToString("clientId:secret".getBytes(StandardCharsets.UTF_8));

  private final WiremockLogNotifier notifier = new WiremockLogNotifier();

  @Test
  @DisplayName("should handle null message gracefully")
  void shouldHandleNullMessage() {
    assertThat(WiremockLogNotifier.sanitize(null)).isNull();
    assertDoesNotThrow(() -> notifier.info(null));
    assertDoesNotThrow(() -> notifier.error(null));
    assertDoesNotThrow(() -> notifier.error(null, new RuntimeException()));
  }

  @Test
  @DisplayName("should redact basic authentication header in wiremock request log format")
  void shouldRedactBasicAuthInWiremockFormat() {
    String message = "Request received:\n"
        + "127.0.0.1 - POST /token/uri\n\n"
        + "Accept-Encoding: [gzip]\n"
        + "Authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]\n"
        + "Content-Type: [application/x-www-form-urlencoded]\n";

    String sanitized = WiremockLogNotifier.sanitize(message);
    assertThat(sanitized).doesNotContain(DUMMY_BASIC_AUTH_TOKEN);
    assertThat(sanitized).doesNotContain("Basic");
    assertThat(sanitized).contains("Authorization: [REDACTED]");
  }

  static Stream<String> provideSensitiveHeaders() {
    return Stream.of(
        "Authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]",
        "authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]",
        "AUTHORIZATION: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]",
        "Authorization: Basic " + DUMMY_BASIC_AUTH_TOKEN,
        "authorization: Basic " + DUMMY_BASIC_AUTH_TOKEN,
        "Proxy-Authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]",
        "proxy-authorization: Basic " + DUMMY_BASIC_AUTH_TOKEN,
        "x-api-key: [secretKey12345]",
        "x-apikey: secretKey12345",
        "Authorization: [Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9]"
    );
  }

  @ParameterizedTest
  @MethodSource("provideSensitiveHeaders")
  @DisplayName("should redact various sensitive headers")
  void shouldRedactVariousSensitiveHeaders(String headerLine) {
    String sanitized = WiremockLogNotifier.sanitize(headerLine);
    assertThat(sanitized).doesNotContain(DUMMY_BASIC_AUTH_TOKEN);
    assertThat(sanitized).doesNotContain("secretKey12345");
    assertThat(sanitized).doesNotContain("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
    assertThat(sanitized).contains("[REDACTED]");
  }

  @Test
  @DisplayName("should redact sensitive headers in json format")
  void shouldRedactSensitiveHeadersInJsonFormat() {
    String json = "{\n  \"Authorization\": \"Basic " + DUMMY_BASIC_AUTH_TOKEN
        + "\",\n  \"x-api-key\": \"abc1234\"\n}";
    String sanitized = WiremockLogNotifier.sanitize(json);
    assertThat(sanitized).doesNotContain(DUMMY_BASIC_AUTH_TOKEN);
    assertThat(sanitized).doesNotContain("abc1234");
    assertThat(sanitized).contains("\"Authorization\": \"[REDACTED]\"");
    assertThat(sanitized).contains("\"x-api-key\": \"[REDACTED]\"");
  }

  @Test
  @DisplayName("should redact standalone basic auth tokens as defense in depth")
  void shouldRedactStandaloneBasicAuth() {
    String standalone = "Failed with token Basic " + DUMMY_BASIC_AUTH_TOKEN + " for user";
    String sanitized = WiremockLogNotifier.sanitize(standalone);
    assertThat(sanitized).doesNotContain(DUMMY_BASIC_AUTH_TOKEN);
    assertThat(sanitized).contains("Basic [REDACTED]");
  }

  @Test
  @DisplayName("should call info and error without exception")
  void shouldCallInfoAndErrorWithoutException() {
    assertDoesNotThrow(() -> notifier.info("Authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]"));
    assertDoesNotThrow(() -> notifier.error("Authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]"));
    assertDoesNotThrow(() -> notifier.error(
        "Authorization: [Basic " + DUMMY_BASIC_AUTH_TOKEN + "]",
        new RuntimeException("err")
    ));
  }
}
