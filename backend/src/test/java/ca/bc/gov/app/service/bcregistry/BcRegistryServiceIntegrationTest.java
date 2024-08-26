package ca.bc.gov.app.service.bcregistry;

import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOCOBJ_ANY;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOCOBJ_GP;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOCOBJ_SP_ORG;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOCOBJ_SP_PERSON;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOCOBJ_XP;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOC_GP;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOC_REQ_FAIL;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOC_REQ_RES;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOC_SP_ORG;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_DOC_SP_PERSON;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_401;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_500;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_ANY;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_EMPTY;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_FAIL;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_GP;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_SP_ORG;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_SP_PERSON;
import static ca.bc.gov.app.BcRegistryTestConstants.BCREG_FACET_XP;
import static ca.bc.gov.app.BcRegistryTestConstants.NO_DATA;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Named.named;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@Slf4j
@DisplayName("Integrated Test | BC Registry Service")
class BcRegistryServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private BcRegistryService service;

  @RegisterExtension
  static WireMockExtension bcRegistryStub = WireMockExtension
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
  public void setUp() {
    bcRegistryStub.resetAll();
  }

  @ParameterizedTest(name = "Search by facets: {0}")
  @MethodSource("byFacets")
  @DisplayName("should search and get facets")
  void shouldSearch(
      String data,
      String response,
      int responseStatus,
      BcRegistryFacetSearchResultEntryDto expected
  ) {

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .withRequestBody(equalToJson(
                        String.format("""
                            {
                              "query": { "value": "%s","name": "%s" },
                              "categories":{ "status":["ACTIVE"] },
                              "rows": 100,
                              "start":0
                            }""", data, data)
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(responseStatus)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)
                )
        );

    FirstStep<BcRegistryFacetSearchResultEntryDto> step =
        service
            .searchByFacets(data)
            .as(StepVerifier::create);

    if (responseStatus != 200) {
      step.expectError(ResponseStatusException.class).verify();
    } else {
      if (expected == null) {
        step.expectNextCount(0).verifyComplete();
      } else {
        step.expectNext(expected).verifyComplete();
      }
    }
  }

  @ParameterizedTest(name = "Get document: {0}")
  @MethodSource("byDocument")
  @DisplayName("should request document")
  void shouldRequestData(
      String data,
      String documentRequestResponse,
      int documentRequestStatus,
      String documentResponseResponse,
      int documentResponseStatus,
      String facetResponse,
      boolean shouldFail,
      BcRegistryDocumentDto expected
  ) {

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(facetResponse)
                )
        );

    bcRegistryStub
        .stubFor(post(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + data + "/documents/requests"))
            .willReturn(
                status(documentRequestStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(documentRequestResponse)
            )
        );

    bcRegistryStub
        .stubFor(get(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + data + "/documents/aa0a00a0a"))
            .willReturn(
                status(documentResponseStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(documentResponseResponse)
            )
        );

    FirstStep<BcRegistryDocumentDto> step =
        service
            .requestDocumentData(data)
            .as(StepVerifier::create);

    if (shouldFail) {
      step.expectError(ResponseStatusException.class).verify();
    } else {
      step.expectNext(expected).verifyComplete();
    }
  }

  private static Stream<Arguments> byFacets() {
    return Stream.of(
        Arguments.of(
            named("Request messed up", "C0123456"),
            BCREG_FACET_FAIL, 400, null
        ),
        Arguments.of(
            named("Wrong key", "C0123456"),
            BCREG_FACET_401, 401, null
        ),
        Arguments.of(
            named("500 error", "C0123456"),
            BCREG_FACET_500, 500, null
        ),
        Arguments.of(
            named("No data", "C0123456"),
            BCREG_FACET_EMPTY, 200, null
        ),
        Arguments.of(
            named("Corporation", "C0123456"),
            BCREG_FACET_ANY, 200,
            new BcRegistryFacetSearchResultEntryDto(
                "100000000000001",
                "C0123456",
                "C",
                "EXAMPLE COMPANY LTD.",
                "ACTIVE",
                true,
                null
            )
        ),
        Arguments.of(
            named("Sole Prop owned by corporation", "FM0123456"),
            BCREG_FACET_SP_ORG, 200,
            new BcRegistryFacetSearchResultEntryDto(
                "100000000000002",
                "FM0123456",
                "SP",
                "EXAMPLE SOLE PROPRIETORSHIP",
                "ACTIVE",
                null,
                List.of(
                    new BcRegistryFacetPartyDto(
                        "EXAMPLE COMPANY LTD.",
                        List.of("proprietor"),
                        "organization"
                    )
                )
            )
        ),
        Arguments.of(
            named("Sole Prop owned by person", "FM0123210"),
            BCREG_FACET_SP_PERSON, 200,
            new BcRegistryFacetSearchResultEntryDto(
                "100000000000003",
                "FM0123210",
                "SP",
                "EXAMPLE SOLE PROPRIETORSHIP",
                "ACTIVE",
                null,
                List.of(
                    new BcRegistryFacetPartyDto(
                        "JOHNATHAN WICK",
                        List.of("proprietor"),
                        "person"
                    )
                )
            )
        ),
        Arguments.of(
            named("General partnership with multiple partners", "FM0123432"),
            BCREG_FACET_GP, 200,
            new BcRegistryFacetSearchResultEntryDto(
                StringUtils.EMPTY,
                "FM0123432",
                "GP",
                "GENERAL PARTNERSHIP",
                "ACTIVE",
                null,
                List.of(
                    new BcRegistryFacetPartyDto(
                        "JOHNATHAN VALELONG WICK",
                        List.of("partner"),
                        "person"
                    ),
                    new BcRegistryFacetPartyDto(
                        "RUSKA ROMA",
                        List.of("partner"),
                        "person"
                    )
                )
            )
        ),
        Arguments.of(
            named("XP means some kind of partnership", "XP0123456"),
            BCREG_FACET_XP, 200,
            new BcRegistryFacetSearchResultEntryDto(
                null,
                "XP0123456",
                "XP",
                "EXAMPLE FUND",
                "ACTIVE",
                null,
                List.of(
                    new BcRegistryFacetPartyDto(
                        "EXAMPLE INVESTMENTS INC.",
                        List.of("partner"),
                        "organization"
                    )
                )
            )
        )
    );
  }

  private static Stream<Arguments> byDocument() {
    return
        Stream.of(
            Arguments.of(
                named("Request messed up", "C0123456"),
                BCREG_FACET_FAIL, 400,
                BCREG_FACET_FAIL, 400,
                NO_DATA, true,
                null
            ),
            Arguments.of(
                named("Wrong key", "C0123456"),
                BCREG_FACET_401, 401,
                BCREG_FACET_401, 401,
                NO_DATA, true,
                null
            ),
            Arguments.of(
                named("500 error", "C0123456"),
                BCREG_FACET_500, 500,
                BCREG_FACET_500, 500,
                NO_DATA, true,
                null
            ),
            Arguments.of(
                named("Company do not have response", "C0123456"),
                BCREG_DOC_REQ_FAIL, 400,
                NO_DATA, 200,
                BCREG_FACET_ANY, false,
                BCREG_DOCOBJ_ANY
            ),
            Arguments.of(
                named("Sole proprietorship owned by person", "FM0123210"),
                BCREG_DOC_REQ_RES, 201,
                BCREG_DOC_SP_PERSON, 200,
                BCREG_FACET_EMPTY, false,
                BCREG_DOCOBJ_SP_PERSON
            ),
            Arguments.of(
                named("Sole proprietorship owned by organization", "FM0123456"),
                BCREG_DOC_REQ_RES, 201,
                BCREG_DOC_SP_ORG, 200,
                BCREG_FACET_EMPTY, false,
                BCREG_DOCOBJ_SP_ORG
            ),
            Arguments.of(
                named("General partnership with multiple owners", "FM0123432"),
                BCREG_DOC_REQ_RES, 201,
                BCREG_DOC_GP, 200,
                BCREG_FACET_EMPTY, false,
                BCREG_DOCOBJ_GP
            ),
            Arguments.of(
                named("XP with a single org owner", "XP0123456"),
                BCREG_DOC_REQ_FAIL, 400,
                NO_DATA, 200,
                BCREG_FACET_XP, false,
                BCREG_DOCOBJ_XP
            )
        );

  }

}