package ca.bc.gov.app.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test | DateTime Converters")
class DateTimeConvertersTest {

  @Test
  @DisplayName("Should convert LocalDateTime to LocalDate")
  void shouldConvertLocalDateTimeToLocalDate() {
    DateTimeConverters.LocalDateTimeToLocalDateConverter converter =
        new DateTimeConverters.LocalDateTimeToLocalDateConverter();

    LocalDateTime dateTime = LocalDateTime.of(2026, 10, 28, 9, 8, 59, 547210116);
    LocalDate date = converter.convert(dateTime);

    assertNotNull(date);
    assertEquals(LocalDate.of(2026, 10, 28), date);
  }

  @Test
  @DisplayName("Should convert LocalDate to LocalDateTime")
  void shouldConvertLocalDateToLocalDateTime() {
    DateTimeConverters.LocalDateToLocalDateTimeConverter converter =
        new DateTimeConverters.LocalDateToLocalDateTimeConverter();

    LocalDate date = LocalDate.of(2026, 10, 28);
    LocalDateTime dateTime = converter.convert(date);

    assertNotNull(dateTime);
    assertEquals(LocalDateTime.of(2026, 10, 28, 0, 0, 0), dateTime);
  }

}

