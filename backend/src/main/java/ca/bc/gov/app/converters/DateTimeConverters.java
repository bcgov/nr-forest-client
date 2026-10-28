package ca.bc.gov.app.converters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Converts between {@link LocalDateTime} and {@link LocalDate} for Spring Data R2DBC.
 */
public final class DateTimeConverters {

  private DateTimeConverters() {
  }

  /**
   * Converts {@link LocalDateTime} to {@link LocalDate}.
   */
  @ReadingConverter
  public static class LocalDateTimeToLocalDateConverter
      implements Converter<LocalDateTime, LocalDate> {

    @Override
    public LocalDate convert(LocalDateTime source) {
      return source.toLocalDate();
    }
  }

  /**
   * Converts {@link LocalDate} to {@link LocalDateTime}.
   */
  @WritingConverter
  public static class LocalDateToLocalDateTimeConverter
      implements Converter<LocalDate, LocalDateTime> {

    @Override
    public LocalDateTime convert(LocalDate source) {
      return source.atStartOfDay();
    }
  }

}

