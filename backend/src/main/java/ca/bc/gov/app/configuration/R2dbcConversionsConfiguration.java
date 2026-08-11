package ca.bc.gov.app.configuration;

import ca.bc.gov.app.converters.SubmissionEnumConverters;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy;
import org.springframework.data.relational.core.mapping.NamingStrategy;

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
 *
 * <p>This class also provides a custom {@link R2dbcMappingContext} that restricts
 * persistent entity creation to application classes only. Without this override, Spring
 * Data R2DBC's AOT processor attempts to create {@code PersistentEntity} instances for
 * internal Netty classes (e.g. {@code io.netty.util.ResourceLeakDetector$TraceRecord})
 * which extend {@code java.lang.Throwable} and fail with an
 * {@code InaccessibleObjectException} because {@code java.base/java.lang} is not opened
 * for reflection in the native build JVM.
 */
@Configuration
public class R2dbcConversionsConfiguration {

  /**
   * Provides a custom {@link R2dbcMappingContext} that only creates
   * {@code PersistentEntity} instances for classes whose package starts with
   * {@code ca.bc.gov.}, preventing third-party and JDK-internal types (such as Netty
   * internals) from being processed during AOT compilation.
   *
   * @param namingStrategy optional naming strategy contributed by auto-configuration
   * @param r2dbcCustomConversions the custom conversions bean (defined below)
   * @return a filtered {@link R2dbcMappingContext}
   */
  @Bean
  @Primary
  public R2dbcMappingContext r2dbcMappingContext(
      Optional<NamingStrategy> namingStrategy,
      R2dbcCustomConversions r2dbcCustomConversions) {

    R2dbcMappingContext context =
        new R2dbcMappingContext(namingStrategy.orElseGet(DefaultNamingStrategy::new)) {
          @Override
          protected boolean shouldCreatePersistentEntityFor(TypeInformation<?> type) {
            String name = type.getType().getName();
            return name.startsWith("ca.bc.gov.")
                && super.shouldCreatePersistentEntityFor(type);
          }
        };
    context.setSimpleTypeHolder(r2dbcCustomConversions.getSimpleTypeHolder());
    return context;
  }

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
