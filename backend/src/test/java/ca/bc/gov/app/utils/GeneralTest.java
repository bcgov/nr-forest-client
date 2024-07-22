package ca.bc.gov.app.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Unit Tests | General and Unrelated Tests")
class GeneralTest {

  @ParameterizedTest
  @MethodSource("notADate")
  void dateIsNotValid(String notADateAtAll) {
    assertThrows(DateTimeException.class, () -> {
      LocalDate.parse(notADateAtAll, DateTimeFormatter.ISO_LOCAL_DATE);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {"mail@mail.ca", "donotnod@happygames.ca", "paulushc@gmail.com"})
  void testMail(String value) {
    String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";
    assertTrue(value.matches(EMAIL_REGEX));
    assertTrue(Pattern.compile(EMAIL_REGEX).matcher(value).matches());
  }

  /**
   * Provide a stream of invalid dates. We don't need to validate a date, as when a date is parsed,
   * it will fail to convert.
   *
   * @return a stream of invalid dates
   */
  private static Stream<Arguments> notADate() {
    return Stream.of(
        Arguments.of("not a date"),
        Arguments.of("2021-13-01"),
        Arguments.of("2021-01-32"),
        Arguments.of("2021-02-30"),
        Arguments.of("2021-02-29"),
        Arguments.of("2021-04-31"),
        Arguments.of("2021-06-31"),
        Arguments.of("2021-09-31"),
        Arguments.of("2021-11-31"),
        Arguments.of("0000-00-00")
    );
  }

}
