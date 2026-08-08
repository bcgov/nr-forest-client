package ca.bc.gov.app.configuration;

import ca.bc.gov.app.converters.SubmissionEnumConverters;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

/**
 * Registers Spring Data R2DBC custom conversions that are no longer provided by default
 * in Spring Data R2DBC 4.x.
 *
 * <p>Spring Boot 4 (Spring Data R2DBC 4.x) removed the automatic {@link Enum} to
 * {@code String} conversion used for binding enum entity columns and query parameters.
 * Without these converters, the {@code io.r2dbc.postgresql} driver rejects enum values
 * with {@code IllegalArgumentException: Cannot encode parameter of type ...}. The
 * converters in {@link SubmissionEnumConverters} restore the previous behaviour for the
 * submission entity enum columns.
 */
@Configuration
public class R2dbcConversionsConfiguration {

  /**
   * Registers the submission enum converters so they are applied on read and write
   * operations against the {@code nrfc.submission} table.
   *
   * <p>The converters are appended to {@link R2dbcCustomConversions#STORE_CONVERSIONS}
   * (the default store conversions shared by every R2DBC dialect) so registering this
   * bean does not depend on the auto-configured {@code R2dbcDialect}, which would create
   * a circular dependency in Spring Boot 4's auto-configuration order.
   *
   * @return the {@link R2dbcCustomConversions} instance wired into the
   *     {@code MappingR2dbcConverter}.
   */
  @Bean
  R2dbcCustomConversions r2dbcCustomConversions() {
     return R2dbcCustomConversions.of(
         PostgresDialect.INSTANCE,
         List.of(
             new SubmissionEnumConverters.SubmissionStatusEnumToString(),
             new SubmissionEnumConverters.StringToSubmissionStatusEnum(),
             new SubmissionEnumConverters.SubmissionTypeCodeEnumToString(),
             new SubmissionEnumConverters.StringToSubmissionTypeCodeEnum()
         ).toArray()
     );
  }
}
