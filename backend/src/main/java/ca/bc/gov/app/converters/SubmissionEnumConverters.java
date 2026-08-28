package ca.bc.gov.app.converters;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.models.client.SubmissionTypeCodeEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Converts between the application's submission enums ({@link SubmissionStatusEnum} and
 * {@link SubmissionTypeCodeEnum}) and the {@code String} codes persisted in the database.
 *
 * <p>Spring Data R2DBC 3.x automatically converted {@link Enum} instances to their
 * {@link Enum#name()} when binding a column or query parameter. Spring Data R2DBC 4.x
 * (used by Spring Boot 4) no longer registers that default converter, so the raw enum
 * reaches the Postgres R2DBC driver, which cannot encode it and throws
 * {@code IllegalArgumentException: Cannot encode parameter of type ...}. These converters
 * restore the previous behaviour by writing the {@link Enum#name()} and reading the enum
 * back through {@link Enum#valueOf(Class, String)}.
 */
public final class SubmissionEnumConverters {

  private SubmissionEnumConverters() {
  }

  /**
   * Writes a {@link SubmissionStatusEnum} to its {@link Enum#name()} (P, A, R, D, N).
   */
  @WritingConverter
  public static class SubmissionStatusEnumToString
      implements Converter<SubmissionStatusEnum, String> {

    @Override
    public String convert(SubmissionStatusEnum source) {
      return source.name();
    }
  }

  /**
   * Reads a {@link SubmissionStatusEnum} from the stored {@link Enum#name()}.
   */
  @ReadingConverter
  public static class StringToSubmissionStatusEnum
      implements Converter<String, SubmissionStatusEnum> {

    @Override
    public SubmissionStatusEnum convert(String source) {
      return SubmissionStatusEnum.valueOf(source);
    }
  }

  /**
   * Writes a {@link SubmissionTypeCodeEnum} to its {@link Enum#name()} (SPP, RNC, ...).
   */
  @WritingConverter
  public static class SubmissionTypeCodeEnumToString
      implements Converter<SubmissionTypeCodeEnum, String> {

    @Override
    public String convert(SubmissionTypeCodeEnum source) {
      return source.name();
    }
  }

  /**
   * Reads a {@link SubmissionTypeCodeEnum} from the stored {@link Enum#name()}.
   */
  @ReadingConverter
  public static class StringToSubmissionTypeCodeEnum
      implements Converter<String, SubmissionTypeCodeEnum> {

    @Override
    public SubmissionTypeCodeEnum convert(String source) {
      return SubmissionTypeCodeEnum.valueOf(source);
    }
  }

}
