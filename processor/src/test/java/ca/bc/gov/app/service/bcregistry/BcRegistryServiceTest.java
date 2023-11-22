package ca.bc.gov.app.service.bcregistry;

import static ca.bc.gov.app.TestConstants.BCREG_DOC_REQ_RES;
import static ca.bc.gov.app.TestConstants.BCREG_RES2;
import static ca.bc.gov.app.TestConstants.BCREG__RES1;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | BC Registry Service")
class BcRegistryServiceTest {

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

  private final BcRegistryService service = new BcRegistryService(
      WebClient
          .builder()
          .baseUrl("http://localhost:10040")
          .build()
  );

  @ParameterizedTest
  @MethodSource("documentData")
  @DisplayName("should read document data")
  void shouldReadDocumentData(
      String clientNumber,
      int requestStatus,
      String requestBody,
      int detailsStatus,
      String detailsBody
  ) {

    bcRegistryStub
        .stubFor(post(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + clientNumber + "/documents/requests"))
            .willReturn(
                status(requestStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(requestBody)
            )
        );

    bcRegistryStub
        .stubFor(get(urlPathEqualTo(
                    "/registry-search/api/v1/businesses/" + clientNumber + "/documents/aa0a00a0a"
                )
            )
                .willReturn(
                    status(detailsStatus)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(detailsBody)
                )
        );

    StepVerifier.FirstStep<BcRegistryDocumentDto> test =
        service
            .requestDocumentData(clientNumber)
            .as(StepVerifier::create);

    if (detailsStatus == 200 && requestStatus == 200) {
      test
          .assertNext(document -> assertTrue(document.getProprietor().isProprietor()))
          .verifyComplete();
    }else{
      test
          .expectError()
          .verify();
    }


  }

  private static Stream<Arguments> documentData() {
    return Stream.of(
        Arguments.of("CP0000001",200,BCREG_DOC_REQ_RES, 200, BCREG__RES1),
        Arguments.of("CP0000002",200,BCREG_DOC_REQ_RES, 404, BCREG_RES2),

        Arguments.of("CP0000002",404,BCREG_RES2, 404, BCREG_RES2),
        Arguments.of("CP0000002",401,BCREG_RES2, 404, BCREG_RES2),
        Arguments.of("CP0000002",400,BCREG_RES2, 404, BCREG_RES2),

        Arguments.of("CP0000002",200,BCREG_DOC_REQ_RES, 401, BCREG_RES2),
        Arguments.of("CP0000002",200,BCREG_DOC_REQ_RES, 400, BCREG_RES2)
    );
  }

}