package ca.bc.gov.app.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@DisplayName("Unit Test | Client Submission Service")
class ClientSubmissionServiceUnitTest {

  private ClientCodeService codeService;
  private R2dbcEntityTemplate template;
  private ForestClientConfiguration configuration;
  private ClientSubmissionService service;

  @BeforeEach
  void setUp() {
    codeService = mock(ClientCodeService.class);
    template = mock(R2dbcEntityTemplate.class);
    configuration = mock(ForestClientConfiguration.class);

    service = new ClientSubmissionService(
        codeService, null, null, null, null, null, null, null, null,
        template, configuration, null
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("Should return empty string when input is null, empty or blank")
  void shouldReturnEmptyStringWhenInputIsBlank(String input) {
    assertEquals("", service.removeProvider(input));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "IDIR\\SLEROY, SLEROY",
      "BCEIDBUSINESS\\Mitchbrown-john, Mitchbrown-john",
      "BCSC\\user123, user123",
      "no_provider_user, no_provider_user"
  })
  @DisplayName("Should remove provider prefix when present")
  void shouldRemoveProviderPrefix(String input, String expected) {
    assertEquals(expected, service.removeProvider(input));
  }

  @Test
  @DisplayName(
      "Regression #2543: Default query criteria must include both N and P pending statuses "
          + "and 7-day cutoff for reviewed types")
  void shouldBuildDefaultQueryCriteriaWithPendingStatuses() {
    when(configuration.getSubmissionLimit()).thenReturn(Duration.ofDays(7));
    when(codeService.getClientTypes()).thenReturn(Mono.just(Map.of()));

    ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
    when(template.count(queryCaptor.capture(), eq(SubmissionEntity.class)))
        .thenReturn(Mono.just(0L));
    when(template.select(any(Query.class), eq(SubmissionEntity.class))).thenReturn(Flux.empty());

    service.listSubmissions(0, 10, null, null, null, null, null)
        .collectList()
        .block();

    verify(template).count(any(Query.class), eq(SubmissionEntity.class));

    CriteriaDefinition criteria = queryCaptor.getValue().getCriteria().orElseThrow();
    String criteriaString = criteria.toString();

    // Verify both pending statuses ('N' and 'P') are present in the query
    assertThat(criteriaString).contains("submissionStatus = 'N'");
    assertThat(criteriaString).contains("submissionStatus = 'P'");

    // Verify the 7-day age cutoff applies to reviewed submission types ('AAC' and 'RNC')
    assertThat(criteriaString).contains("updateTimestamp >=");
    assertThat(criteriaString).contains("submissionType = 'AAC'");
    assertThat(criteriaString).contains("submissionType = 'RNC'");
  }

}
