package ca.bc.gov.app.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.entity.client.EmailLogEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Submission Match Detail Entity Before Convert")
class EmailLogEntityJsonConvertTest {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final EmailLogEntityJsonConvert sut = new EmailLogEntityJsonConvert(
      mapper);

  @ParameterizedTest
  @MethodSource("onBefore")
  @DisplayName("CASE 1: onBeforeConvert")
  void shouldOnBeforeConvert(EmailLogEntity value,
      EmailLogEntity expected) {

    Mono.from(sut
            .onBeforeConvert(value, SqlIdentifier.unquoted("table"))
        )
        .as(StepVerifier::create)
        .assertNext(actual -> {
          assertEquals(expected.getEmailVariables().asString(), actual.getEmailVariables().asString());
          if(expected.getVariables() != null)
            assertEquals(expected.getVariables(), actual.getVariables());
        })
        .verifyComplete();

  }

  @ParameterizedTest
  @MethodSource("onAfter")
  @DisplayName("CASE 2: onAfterConvert")
  void shouldOnAfterConvert(EmailLogEntity value,
      EmailLogEntity expected) {

    Mono.from(sut
            .onAfterConvert(value, SqlIdentifier.unquoted("table"))
        )
        .as(StepVerifier::create)
        .assertNext(actual ->
            assertEquals(expected.getVariables(), actual.getVariables())
        )
        .verifyComplete();

  }

  private static Stream<Arguments> onBefore() throws JsonProcessingException {
    return Stream
        .of(
            Arguments.of(
                EmailLogEntity.builder().build(),
                EmailLogEntity.builder().emailVariables(Json.of("{}")).build()
            ),
            Arguments.of(
                EmailLogEntity.builder().variables(Map.of()).build(),
                EmailLogEntity.builder().emailVariables(Json.of("{}")).build()
            ),
            Arguments.of(
                EmailLogEntity.builder().variables(Map.of("potato", "potato")).build(),
                EmailLogEntity.builder()
                    .emailVariables(
                        Json.of(
                            mapper.writeValueAsString(
                                Map.of("potato", "potato")
                            )
                        )
                    )
                    .variables(Map.of("potato", "potato"))
                    .build()
            )
        );
  }

  private static Stream<Arguments> onAfter() throws JsonProcessingException {
    return Stream
        .of(
            Arguments.of(
                EmailLogEntity.builder().build(),
                EmailLogEntity.builder().variables(Map.of()).build()
            ),
            Arguments.of(
                EmailLogEntity.builder().emailVariables(Json.of("{}")).build(),
                EmailLogEntity.builder().variables(Map.of()).build()
            ),
            Arguments.of(
                EmailLogEntity.builder().emailVariables(Json.of("")).build(),
                EmailLogEntity.builder().variables(Map.of()).build()
            ),
            Arguments.of(
                EmailLogEntity.builder()
                    .emailVariables(
                        Json.of(
                            mapper.writeValueAsString(
                                Map.of("potato", "potato")
                            )
                        )
                    )
                    .build(),
                EmailLogEntity.builder().variables(Map.of("potato", "potato")).build()
            )
        );
  }

}