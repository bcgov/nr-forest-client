package ca.bc.gov.app.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.Message;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessorUtil {

  @SuppressWarnings("unchecked:java:S3740")
  public static <T> Optional<T> readHeader(
      Message message,
      String headerName, Class<T> clazz
  ) {
    return Optional.ofNullable(message.getHeaders().get(headerName, clazz));
  }

  public static String extractLetters(String input) {
    Pattern pattern = Pattern.compile("[A-Z]+");
    return getStringFromPattern(input, pattern);
  }

  public static String extractNumbers(String input) {
    Pattern pattern = Pattern.compile("\\d+");
    return getStringFromPattern(input, pattern);
  }

  private static String getStringFromPattern(String input, Pattern pattern) {
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      return matcher.group();
    } else {
      return StringUtils.EMPTY;
    }
  }


}
