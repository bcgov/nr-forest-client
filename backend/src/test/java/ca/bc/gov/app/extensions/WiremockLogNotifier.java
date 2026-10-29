package ca.bc.gov.app.extensions;

import com.github.tomakehurst.wiremock.common.Notifier;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WiremockLogNotifier implements Notifier {

  private static final Pattern SENSITIVE_HEADERS_PATTERN = Pattern.compile(
      "(?i)(authorization|proxy-authorization|x-api-key|x-apikey)\\s*:\\s*(?:\\[[^\\]\\r\\n]*\\]|[^\\r\\n]*)",
      Pattern.MULTILINE
  );

  private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
      "(?i)([\"'](?:authorization|proxy-authorization|x-api-key|x-apikey)[\"']\\s*:\\s*[\"'])[^\"'\\r\\n]+([\"'])"
  );

  private static final Pattern BASIC_AUTH_PATTERN = Pattern.compile(
      "(?i)\\b(basic\\s+)[a-zA-Z0-9+/=]{6,}"
  );

  @Override
  public void info(String message) {
    log.info(sanitize(message));
  }

  @Override
  public void error(String message) {
    log.error(sanitize(message));
  }

  @Override
  public void error(String message, Throwable t) {
    log.error(sanitize(message), t);
  }

  public static String sanitize(String message) {
    if (message == null) {
      return null;
    }
    String sanitized = SENSITIVE_HEADERS_PATTERN.matcher(message).replaceAll("$1: [REDACTED]");
    sanitized = SENSITIVE_JSON_PATTERN.matcher(sanitized).replaceAll("$1[REDACTED]$2");
    sanitized = BASIC_AUTH_PATTERN.matcher(sanitized).replaceAll("$1[REDACTED]");
    return sanitized;
  }
}
