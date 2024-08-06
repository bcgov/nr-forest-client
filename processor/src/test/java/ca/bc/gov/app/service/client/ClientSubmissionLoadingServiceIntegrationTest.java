package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.DistrictDto;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Service")
@Slf4j
class ClientSubmissionLoadingServiceIntegrationTest extends AbstractTestContainer {

  @Autowired
  ClientSubmissionLoadingService service;

  @RegisterExtension
  static WireMockExtension backendStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10000)
              .stubRequestLoggingDisabled(false)
              .disableRequestJournal()
              .notifier(new Slf4jNotifier(true) )
      )
      .configureStaticDsl(true)
      .build();

  @ParameterizedTest
  @MethodSource("mailBody")
  @DisplayName("get mail message")
  void shouldCreateCorrectMailMessage(
      Integer submissionId,
      SubmissionStatusEnum status,
      String emailAddresses
  ) {

    backendStub.resetAll();
    backendStub
        .stubFor(
            get("/codes/districts/DCR")
                .withBasicAuth("uat", "thisisasupersecret")
                .willReturn(
                    aResponse()
                        .withBody("{\"code\": \"DCR\", \"description\": \"Test District\",\"emails\":\"alliance@mail.ca\"}")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                )
        );


    MessagingWrapper<Integer> message = new MessagingWrapper<>(submissionId,
        Map.of(
            ApplicationConstant.SUBMISSION_STATUS, status,
            ApplicationConstant.MATCHING_REASON, "reason"
        )

    );
    service
        .buildMailMessage(message)
        .as(StepVerifier::create)
        .assertNext(emailRequestDto ->
            assertThat(emailRequestDto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", emailAddresses)
        )
        .verifyComplete();

  }


  private static Stream<Arguments> mailBody() {
    return Stream.of(
        Arguments.of(365, SubmissionStatusEnum.N, "alliance@mail.ca"),
        Arguments.of(366, SubmissionStatusEnum.A, "alliance@mail.ca,uattestingmail@uat.testing.lo"),
        Arguments.of(367, SubmissionStatusEnum.R, "alliance@mail.ca,uattestingmail@uat.testing.lo")
    );
  }

}
