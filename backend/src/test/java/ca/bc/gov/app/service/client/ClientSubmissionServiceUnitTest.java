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
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.models.client.SubmissionTypeCodeEnum;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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

    service =
        new ClientSubmissionService(
            codeService,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            template,
            configuration,
            null);
  }

  @ParameterizedTest
  @CsvSource({"IDIR\\username,username", "BCEIDBUSINESS\\testuser,testuser", "test,test"})
  @DisplayName("Should extract username without provider prefix")
  void shouldRemoveProviderPrefix(String input, String expected) {
    assertEquals(expected, service.removeProvider(input));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("Should handle null or blank provider prefix")
  void shouldHandleBlankProviderPrefix(String input) {
    assertEquals("", service.removeProvider(input));
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

    // Directly assert the criteria's typed enum values
    List<Object> criteriaValues = unrollCriteria(criteria)
        .stream()
        .map(CriteriaDefinition::getValue)
        .toList();

    assertThat(criteriaValues)
        .contains(
            SubmissionStatusEnum.N,
            SubmissionStatusEnum.P,
            SubmissionTypeCodeEnum.AAC,
            SubmissionTypeCodeEnum.RNC
        );

    // Verify query structure supports both quoted and unquoted rendering
    assertThat(criteriaString)
        .contains("updateTimestamp >=")
        .matches(s -> s.contains("submissionStatus = 'N'") || s.contains("submissionStatus = N"))
        .matches(s -> s.contains("submissionStatus = 'P'") || s.contains("submissionStatus = P"))
        .matches(s -> s.contains("submissionType = 'AAC'") || s.contains("submissionType = AAC"))
        .matches(s -> s.contains("submissionType = 'RNC'") || s.contains("submissionType = RNC"));
  }

  private static List<CriteriaDefinition> unrollCriteria(CriteriaDefinition criteria) {
    List<CriteriaDefinition> list = new ArrayList<>();
    CriteriaDefinition current = criteria;
    while (current != null) {
      if (current.isGroup()) {
        for (CriteriaDefinition child : current.getGroup()) {
          list.addAll(unrollCriteria(child));
        }
      } else {
        list.add(current);
      }
      current = current.hasPrevious() ? current.getPrevious() : null;
    }
    return list;
  }

}
