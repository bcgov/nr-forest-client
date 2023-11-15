package ca.bc.gov.app.util;

import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.integration.support.MessageBuilder;

@DisplayName("Unit Test | Processor Util")
class ProcessorUtilTest {


  @ParameterizedTest(name = "should read header {0} and get {1}")
  @MethodSource("readHeader")
  @DisplayName("should read header")
  void shouldReadHeader(String headerName, Optional<String> expected) {
    Assertions
        .assertEquals(expected, ProcessorUtil
            .readHeader(
                MessageBuilder
                    .withPayload("test")
                    .setHeader("pizza", "cheese")
                    .build(),
                headerName,
                String.class));
  }

  @ParameterizedTest(name = "should extract letters from {0} and get {1}")
  @MethodSource("extract")
  @DisplayName("should extract letters")
  void shouldExtractLetters(String input, String expectedLetters, String expectedNumbers) {
    Assertions.assertEquals(expectedLetters, ProcessorUtil.extractLetters(input));
  }

  @ParameterizedTest(name = "should extract numbers from {0} and get {2}")
  @MethodSource("extract")
  @DisplayName("should extract numbers")
  void shouldExtractNumbers(String input, String expectedLetters, String expectedNumbers) {
    Assertions.assertEquals(expectedNumbers, ProcessorUtil.extractNumbers(input));
  }

  @ParameterizedTest(name = "should split name {0} and get {1}")
  @MethodSource("splitName")
  @DisplayName("should split name")
  void shouldSplitName(String input, String[] expected) {
    Assertions.assertArrayEquals(expected, ProcessorUtil.splitName(input));
  }

  private static Stream<Arguments> readHeader() {
    return Stream.of(
        Arguments.of("party", Optional.empty()),
        Arguments.of("pizza", Optional.of("cheese")),
        Arguments.of(StringUtils.EMPTY, Optional.empty())
    );
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        Arguments.of("ABC1234", "ABC", "1234"),
        Arguments.of("ABC", "ABC", ""),
        Arguments.of("1234", "", "1234"),
        Arguments.of("ABC1234ABC", "ABC", "1234")
    );
  }

  private static Stream<Arguments> splitName() {
    return
        Stream.of(
            Arguments.of(
                "John Doe",
                new String[]{"Doe", "John", ""}
            ),
            Arguments.of(
                "Doe, John",
                new String[]{"Doe", "John", ""}
            ),
            Arguments.of(
                "John Doe Smith",
                new String[]{"Smith", "John", "Doe"}
            ),
            Arguments.of(
                "Doe, John Smith",
                new String[]{"Doe", "John", "Smith"}
            ),
            Arguments.of(
                "John Doe Smith Jones",
                new String[]{"Jones", "John", "Doe Smith"}
            ),
            Arguments.of("Jhon", new String[]{"Jhon", "Jhon", ""}),
            Arguments.of(StringUtils.EMPTY,
                new String[]{StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY}
            ),
            Arguments.of(null,
                new String[]{StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY}
            ),
            Arguments.of("          ",
                new String[]{StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY}
            )
        );
  }

}