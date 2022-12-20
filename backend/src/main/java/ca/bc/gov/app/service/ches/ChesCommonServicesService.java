package ca.bc.gov.app.service.ches;

import ca.bc.gov.app.configuration.ChesConfiguration;
import ca.bc.gov.app.dto.ches.ChesMailBodyType;
import ca.bc.gov.app.dto.ches.ChesMailEncoding;
import ca.bc.gov.app.dto.ches.ChesMailPriority;
import ca.bc.gov.app.dto.ches.ChesMailRequest;
import ca.bc.gov.app.dto.ches.ChesMailResponse;
import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.exception.CannotExtractTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChesCommonServicesService {

  private final ChesConfiguration configuration;
  private final OAuthClient oauthClient = new OAuthClient(new URLConnectionClient());
  private final WebClient webClient = WebClient.builder().build();


  public Mono<String> sendEmail(ChesRequest requestContent) {

    ChesMailRequest request = new ChesMailRequest(
        null,
        null,
        ChesMailBodyType.HTML,
        requestContent.emailBody(),
        null,
        0,
        ChesMailEncoding.UTF_8,
        "FSA_donotreply@gov.bc.ca",
        ChesMailPriority.NORMAL,
        "Forest Client Application Confirmation",
        null,
        requestContent.emailTo()
    );

    return
        webClient
            .post()
            .uri(configuration.getUri())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(request), ChesMailRequest.class)
            .exchangeToMono(response -> response.bodyToMono(ChesMailResponse.class))
            .map(response -> response.txId().toString())
            .doOnError(e -> log.error("Failed to send email" + e.toString()));

  }

  private String getToken() {

    try {

      OAuthClientRequest request =
          OAuthClientRequest
              .tokenLocation(configuration.getTokenUrl().toString())
              .setGrantType(GrantType.CLIENT_CREDENTIALS)
              .setClientId(configuration.getClientId())
              .setClientSecret(configuration.getClientSecret())
              .setScope(configuration.getScope())
              .buildBodyMessage();

      return oauthClient
          .accessToken(request, OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class)
          .getAccessToken();
    } catch (Exception e) {
      log.error("Failed to get email authentication token", e);
      throw new CannotExtractTokenException();
    }

  }

}
