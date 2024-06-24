package ca.bc.gov.app.controller.client;

import static ca.bc.gov.app.TestConstants.REGISTERED_BUSINESS_SUBMISSION_DTO;
import static ca.bc.gov.app.TestConstants.UNREGISTERED_BUSINESS_SUBMISSION_BROKEN_DTO;
import static ca.bc.gov.app.TestConstants.UNREGISTERED_BUSINESS_SUBMISSION_DTO;
import static ca.bc.gov.app.TestConstants.UNREGISTERED_BUSINESS_SUBMISSION_MULTI_DTO;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.submissions.SubmissionApproveRejectDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.utilss.ClientSubmissionAggregator;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | FSA Client Submission Controller")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientSubmissionControllerIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;
  @Autowired
  private SubmissionContactRepository contactRepository;

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

  @RegisterExtension
  static WireMockExtension chesStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10010)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @RegisterExtension
  static WireMockExtension legacyStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10060)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void init() {
    bcRegistryStub.resetAll();
    bcRegistryStub
        .stubFor(post(
            urlPathEqualTo("/registry-search/api/v1/businesses/1234/documents/requests"))
            .willReturn(
                status(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(TestConstants.BCREG_DETAIL_OK)
            )
        );

    chesStub
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    chesStub
        .stubFor(
            post("/token/uri")
                .willReturn(
                    ok(TestConstants.CHES_TOKEN_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );
    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/idAndLastName"))
                .withQueryParam("clientId", equalTo("BCEIDBUSINESS\\jdoe"))
                .withQueryParam("lastName", equalTo("Doe"))
                .willReturn(okJson(TestConstants.LEGACY_EMPTY))
        );

    client = client.mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("Submit Registered Business client data")
  @Order(1)
  void shouldSubmitRegisteredBusinessData() {
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(REGISTERED_BUSINESS_SUBMISSION_DTO), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().location("/api/clients/submissions/1")
        .expectHeader().valueEquals("x-sub-id", "1")
        .expectBody().isEmpty();
  }

  @Test
  @DisplayName("Submit Unregistered Business client data")
  @Order(4)
  void shouldSubmitUnregisteredBusinessData() {
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(UNREGISTERED_BUSINESS_SUBMISSION_DTO), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().location("/api/clients/submissions/2")
        .expectHeader().valueEquals("x-sub-id", "2")
        .expectBody().isEmpty();
  }

  @DisplayName("Fail Validation")
  @ParameterizedTest
  @CsvFileSource(resources = "/failValidationTest.csv", numLinesToSkip = 1)
  @Order(3)
  void shouldFailValidationSubmit(
      @AggregateWith(ClientSubmissionAggregator.class) ClientSubmissionDto clientSubmissionDto) {
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
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

    BodyContentSpec expectedBody =
        client
            .mutateWith(csrf())
            .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
            .mutateWith(
                mockJwt()
                    .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                    .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.ROLE_EDITOR))
            )
            .get()
            .uri(uri)
            .header(ApplicationConstant.USERID_HEADER, "testUserId")
            .header(ApplicationConstant.USERMAIL_HEADER, "test@mail.ca")
            .header(ApplicationConstant.USERNAME_HEADER, "Jhon Doe")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(System.out::println);

    if (!found) {
      expectedBody.json(TestConstants.SUBMISSION_LIST_CONTENT_EMPTY);
    } else {

      expectedBody
          .jsonPath("$.[0]").isNotEmpty();
    }
  }

  @Test
  @DisplayName("Submission Details")
  @Order(5)
  void shouldGetSubmissionDetails() {

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                    .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.ROLE_EDITOR))
        )
        .get()
        .uri("/api/clients/submissions/1")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.submissionId").isEqualTo(1)
        .jsonPath("$.updateUser").isEqualTo("jdoe")
        .jsonPath("$.submissionType").isEqualTo("Submission pending processing");
  }

  @Test
  @DisplayName("Submission Approval / Rejection")
  @Order(6)
  void shouldApproveOrReject() {

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                    .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.ROLE_EDITOR))
        )
        .post()
        .uri("/api/clients/submissions/1")
        .body(Mono.just(new SubmissionApproveRejectDto(true, List.of(), null)),
            SubmissionApproveRejectDto.class)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody()
        .isEmpty();
  }

  @Test
  @DisplayName("Submit broken Unregistered Business client data")
  @Order(7)
  void shouldSubmitBrokenUnregisteredBusinessData() {
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(UNREGISTERED_BUSINESS_SUBMISSION_BROKEN_DTO), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBodyList(ValidationError.class)
        .hasSize(1)
        .contains(new ValidationError("businessInformation.businessName",
            "Business name must be composed of first and last name"));
  }

  @Test
  @DisplayName("Submit Unregistered Business client with multiple contacts")
  @Order(8)
  void shouldSubmitMultipleContacts() {
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(UNREGISTERED_BUSINESS_SUBMISSION_MULTI_DTO), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().location("/api/clients/submissions/3")
        .expectHeader().valueEquals("x-sub-id", "3")
        .expectBody().isEmpty();


    contactRepository
        .findAll()
        .filter(contact -> contact.getSubmissionId().intValue() == 3)
        .sort(Comparator.comparing(SubmissionContactEntity::getContactTypeCode))
        .as(StepVerifier::create)
        .assertNext(contact ->
            assertThat(contact)
                .hasFieldOrPropertyWithValue("firstName", "James")
                .hasFieldOrPropertyWithValue("lastName", "Baxter")
                .hasFieldOrPropertyWithValue("emailAddress", "jbaxter@007.com")
                .hasFieldOrPropertyWithValue("businessPhoneNumber","9826543210")
                .hasFieldOrPropertyWithValue("userId", null)
                .hasFieldOrPropertyWithValue("contactTypeCode","BL")
        )
        .assertNext(contact ->
            assertThat(contact)
                .hasFieldOrPropertyWithValue("firstName", "James")
                .hasFieldOrPropertyWithValue("lastName", "Bond")
                .hasFieldOrPropertyWithValue("emailAddress", "bond_james_bond@007.com")
                .hasFieldOrPropertyWithValue("businessPhoneNumber","9876543210")
                .hasFieldOrPropertyWithValue("userId", "BCEIDBUSINESS\\jdoe")
                .hasFieldOrPropertyWithValue("contactTypeCode","LP")
        )
        .verifyComplete();
  }

  @Test
  @DisplayName("Submission Approval / Rejection again fails")
  @Order(9)
  void shouldNotApproveRejectAgain(){
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                    .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.ROLE_EDITOR))
        )
        .post()
        .uri("/api/clients/submissions/1")
        .body(Mono.just(new SubmissionApproveRejectDto(true, List.of(), null)),
            SubmissionApproveRejectDto.class)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectBody(String.class)
        .isEqualTo("This submission was already processed");
  }

  private static Stream<Arguments> listValues() {
    return
        Stream.of(
            Arguments.of(null, null, null, null, true),
            Arguments.of(null, null, 0, null, true),
            Arguments.of(null, null, 0, 10, true),
            Arguments.of(null, null, null, 10, true),
            Arguments.of("requestStatus", "N", null, null, true),
            Arguments.of("clientType", "RSP", null, null, true),
            Arguments.of("name", "Goldfinger", null, null, true),
            Arguments.of("name", "Auric", null, null, false),
            Arguments.of(null, null, 1, null, false),
            Arguments.of(null, null, 1, 1, true),
            Arguments.of(null, null, 99, 1, false)
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
