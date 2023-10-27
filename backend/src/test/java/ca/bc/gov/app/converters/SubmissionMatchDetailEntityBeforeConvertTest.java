package ca.bc.gov.app.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
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
class SubmissionMatchDetailEntityBeforeConvertTest {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final SubmissionMatchDetailEntityBeforeConvert sut = new SubmissionMatchDetailEntityBeforeConvert(
      mapper);

  @ParameterizedTest
  @MethodSource("onBefore")
  @DisplayName("CASE 1: onBeforeConvert")
  void shouldOnBeforeConvert(SubmissionMatchDetailEntity value,
      SubmissionMatchDetailEntity expected) {

    Mono.from(sut
            .onBeforeConvert(value, SqlIdentifier.unquoted("table"))
        )
        .as(StepVerifier::create)
        .assertNext(actual -> {
          assertEquals(expected.getMatchingField().asString(), actual.getMatchingField().asString());
          if(expected.getMatchers() != null)
            assertEquals(expected.getMatchers(), actual.getMatchers());
        })
        .verifyComplete();

  }

  @ParameterizedTest
  @MethodSource("onAfter")
  @DisplayName("CASE 2: onAfterConvert")
  void shouldOnAfterConvert(SubmissionMatchDetailEntity value,
      SubmissionMatchDetailEntity expected) {

    Mono.from(sut
            .onAfterConvert(value, SqlIdentifier.unquoted("table"))
        )
        .as(StepVerifier::create)
        .assertNext(actual ->
            assertEquals(expected.getMatchers(), actual.getMatchers())
        )
        .verifyComplete();

  }

  private static Stream<Arguments> onBefore() throws JsonProcessingException {
    return Stream
        .of(
            Arguments.of(
                SubmissionMatchDetailEntity.builder().build(),
                SubmissionMatchDetailEntity.builder().matchingField(Json.of("{}")).build()
            ),
            Arguments.of(
                SubmissionMatchDetailEntity.builder().matchers(Map.of()).build(),
                SubmissionMatchDetailEntity.builder().matchingField(Json.of("{}")).build()
            ),
            Arguments.of(
                SubmissionMatchDetailEntity.builder().matchers(Map.of("potato", "potato")).build(),
                SubmissionMatchDetailEntity.builder()
                    .matchingField(
                        Json.of(
                            mapper.writeValueAsString(
                                Map.of("potato", "potato")
                            )
                        )
                    )
                    .matchers(Map.of("potato", "potato"))
                    .build()
            )
        );
  }

  private static Stream<Arguments> onAfter() throws JsonProcessingException {
    return Stream
        .of(
            Arguments.of(
                SubmissionMatchDetailEntity.builder().build(),
                SubmissionMatchDetailEntity.builder().matchers(Map.of()).build()
            ),
            Arguments.of(
                SubmissionMatchDetailEntity.builder().matchingField(Json.of("{}")).build(),
                SubmissionMatchDetailEntity.builder().matchers(Map.of()).build()
            ),
            Arguments.of(
                SubmissionMatchDetailEntity.builder().matchingField(Json.of("")).build(),
                SubmissionMatchDetailEntity.builder().matchers(Map.of()).build()
            ),
            Arguments.of(
                SubmissionMatchDetailEntity.builder()
                    .matchingField(
                        Json.of(
                            mapper.writeValueAsString(
                                Map.of("potato", "potato")
                            )
                        )
                    )
                    .build(),
                SubmissionMatchDetailEntity.builder().matchers(Map.of("potato", "potato")).build()
            )
        );
  }

}