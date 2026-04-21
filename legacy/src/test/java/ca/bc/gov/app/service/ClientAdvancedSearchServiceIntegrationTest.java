package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ClientAdvancedSearchCriteriaDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@DisplayName("Integrated Test | Client Advanced Search Service")
class ClientAdvancedSearchServiceIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientAdvancedSearchService service;

  @DisplayName("should perform advanced search with various criteria")
  @ParameterizedTest
  @MethodSource("advancedSearchCases")
  void shouldPerformAdvancedSearch(
      ClientAdvancedSearchCriteriaDto criteria,
      List<String> expectedClientNumbers,
      Class<RuntimeException> exception) {

    FirstStep<Pair<PredictiveSearchResultDto, Long>> test =
        service.advancedSearch(criteria, PageRequest.of(0, 5))
            .as(StepVerifier::create);

    verifyTestData(expectedClientNumbers, exception, test);
  }

  private void verifyTestData(
      List<String> expectedList,
      Class<RuntimeException> exception,
      FirstStep<Pair<PredictiveSearchResultDto, Long>> test) {

    if (expectedList != null && !expectedList.isEmpty()) {
      for (String expected : expectedList) {
        if (StringUtils.isNotBlank(expected)) {
          test.assertNext(
              dto -> {
                org.junit.jupiter.api.Assertions.assertNotNull(dto);
                org.junit.jupiter.api.Assertions.assertEquals(
                    expected, dto.getKey().clientNumber());
              });
        }
      }
    }

    if (exception != null) {
      test.expectError(exception);
    }

    test.verifyComplete();
  }

  private static Stream<Arguments> advancedSearchCases() {
    return Stream.of(
        Arguments.of(null, null, MissingRequiredParameterException.class),
        Arguments.of(
            new ClientAdvancedSearchCriteriaDto(
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null,
                null, 
                null, 
                null, 
                null, 
                null),
            null,
            MissingRequiredParameterException.class),
        Arguments.of(
            new ClientAdvancedSearchCriteriaDto(
                "REICHERT, KILBACK AND EMARD", 
                null, 
                null,
                null, 
                null, 
                null, 
                null, 
                null,
                null, 
                null, 
                null, 
                null),
            List.of("00000123"),
            null),
        Arguments.of(
            new ClientAdvancedSearchCriteriaDto(
                null, 
                "JOE", 
                null, 
                "ACT",
                null, 
                null, 
                null, 
                null,
                null, 
                null, 
                null, 
                null),
            List.of(),
            null),
        Arguments.of(
            new ClientAdvancedSearchCriteriaDto(
                null, 
                null, 
                null, 
                null,
                null, 
                null, 
                null, 
                "john@example.com",
                null, 
                null, 
                null, 
                null),
            List.of(),
            null),
        Arguments.of(
            new ClientAdvancedSearchCriteriaDto(
                null, 
                null, 
                null, 
                null,
                null, 
                null, 
                null, 
                null,
                null, 
                null,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31)),
            List.of(),
            null));
  }
  
}
