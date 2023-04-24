package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ca.bc.gov.app.utils.ClientSubmissionAggregator;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@DisplayName("Integrated Test | FSA Client Submission Controller")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientSubmissionControllerIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10040)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void init() {
    wireMockExtension.resetAll();
    wireMockExtension
        .stubFor(post(
            urlPathEqualTo("/registry-search/api/v1/businesses/1234/documents/requests"))
            .willReturn(
                status(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(TestConstants.BCREG_DETAIL_OK)
            )
        );
  }

  @Test
  @DisplayName("Submit client data")
  @Order(1)
  void shouldSubmitClientData() {
    ClientSubmissionDto clientSubmissionDto =
        new ClientSubmissionDto(
            "testUserId",
            new ClientBusinessInformationDto(
                "1234",
                "Goldfinger",
                BusinessTypeEnum.R,
                ClientTypeEnum.P,
                "Y",
                LegalTypeEnum.GP
            ),
            new ClientLocationDto(
                List.of(
                    new ClientAddressDto(
                        "3570 S Las Vegas Blvd",
                        new ClientValueTextDto("US", ""),
                        new ClientValueTextDto("NV", ""),
                        "Las Vegas", "89109",
                        0,
                        List.of(
                            new ClientContactDto(
                                new ClientValueTextDto("LP","LP"), 
                                "James", 
                                "Bond",
                                "9876543210", 
                                "bond_james_bond@007.com", 
                                0
                            )
                        )
                    )
                )
            ),
            new ClientSubmitterInformationDto(
                "James", 
                "Bond", 
                "1234567890",
                "james_bond@MI6.com"
            )
        );

    client
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(clientSubmissionDto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().location("/api/clients/submissions/1")
        .expectHeader().valueEquals("x-sub-id", "1")
        .expectBody().isEmpty();
  }

  @DisplayName("Fail Validation")
  @ParameterizedTest
  @CsvFileSource(resources = "/failValidationTest.csv", numLinesToSkip = 1)
  @Order(3)
  void shouldFailValidationSubmit(
      @AggregateWith(ClientSubmissionAggregator.class) ClientSubmissionDto clientSubmissionDto) {
    System.out.println(clientSubmissionDto);
    client
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(clientSubmissionDto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().valueEquals("Reason", "Validation failed");
  }

  @ParameterizedTest
  @MethodSource("listValues")
  @DisplayName("List and Search")
  @Order(2)
  void shouldListAndSearch(
      String paramName,
      String paramValue,
      Integer page,
      Integer size,
      boolean found
  ) {

    Function<UriBuilder, URI> uri = uriBuilder ->
        addQuery((page != null), "page", page)
            .andThen(
                addQuery((size != null), "size", size)
            )
            .andThen(
                addQuery(StringUtils.isNotBlank(paramName), paramName, paramValue)
            )
            .apply(uriBuilder.path("/api/clients/submissions"))
            .build(Map.of());

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(found ?
            String.format(TestConstants.SUBMISSION_LIST_CONTENT, LocalDate.now().format(
                DateTimeFormatter.ISO_DATE)) :
            TestConstants.SUBMISSION_LIST_CONTENT_EMPTY
        );
  }

  private static Stream<Arguments> listValues() {
    return
        Stream.of(
            Arguments.of(null,null,null,null,true),
            Arguments.of(null,null,0,null,true),
            Arguments.of(null,null,0,10,true),
            Arguments.of(null,null,null,10,true),
            Arguments.of("requestStatus","P",null,null,true),
            Arguments.of("clientType","P",null,null,true),
            Arguments.of("name","Goldfinger",null,null,true),
            Arguments.of("name","Auric",null,null,false),
            Arguments.of(null,null,1,null,false),
            Arguments.of(null,null,1,1,false)
        );
  }

  private static UnaryOperator<UriBuilder> addQuery(
      boolean shouldAdd,
      String paramName,
      Object paramValue
  ) {
    if (shouldAdd) {
      return builder -> builder.queryParam(paramName, paramValue);
    }
    return UnaryOperator.identity();
  }

}
