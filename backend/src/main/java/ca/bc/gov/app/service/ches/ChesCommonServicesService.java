package ca.bc.gov.app.service.ches;

import ca.bc.gov.app.configuration.ChesConfiguration;
import ca.bc.gov.app.dto.ches.ChesMailBodyType;
import ca.bc.gov.app.dto.ches.ChesMailEncoding;
import ca.bc.gov.app.dto.ches.ChesMailErrorResponse;
import ca.bc.gov.app.dto.ches.ChesMailPriority;
import ca.bc.gov.app.dto.ches.ChesMailRequest;
import ca.bc.gov.app.dto.ches.ChesMailResponse;
import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.exception.BadRequestException;
import ca.bc.gov.app.exception.CannotExtractTokenException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.InvalidRoleException;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.exception.UnexpectedErrorException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ChesCommonServicesService {

  private final ChesConfiguration configuration;
  private final WebClient webClient;

  public ChesCommonServicesService(ChesConfiguration configuration,
                                   @Qualifier("chesApi") WebClient webClient) {
    this.configuration = configuration;
    this.webClient = webClient;
  }

  @Setter
  private OAuthClient oauthClient = new OAuthClient(new URLConnectionClient());

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
            .retrieve()
            .onStatus(httpStatusCode -> httpStatusCode.value() == 401,
                response -> Mono.error(new InvalidAccessTokenException()))
            .onStatus(httpStatusCode -> httpStatusCode.value() == 403,
                response -> Mono.error(new InvalidRoleException()))

            .onStatus(httpStatusCode -> httpStatusCode.value() == 400, get400ErrorMessage())
            .onStatus(httpStatusCode -> httpStatusCode.value() == 422, get422ErrorMessage())
            .onStatus(HttpStatusCode::isError, get500ErrorMessage())
            .bodyToMono(ChesMailResponse.class)

            .map(response -> response.txId().toString())
            .doOnError(error -> log.error("Failed to send email", error));

  }

  private static Function<ClientResponse, Mono<? extends Throwable>> get500ErrorMessage() {
    return response ->
        response
            .bodyToMono(ChesMailErrorResponse.class)
            .flatMap(errorMessageDetail -> Mono.error(
                new UnexpectedErrorException(errorMessageDetail.status(),
                    errorMessageDetail.detail())));
  }

  private static Function<ClientResponse, Mono<? extends Throwable>> get422ErrorMessage() {
    return response ->
        response
            .bodyToMono(ChesMailErrorResponse.class)
            .map(details ->
                Optional
                    .ofNullable(details.errors())
                    .map(errorList -> errorList
                        .stream()
                        .map(errorEntry -> String.format("%s on %s", errorEntry.message(),
                            errorEntry.value()))
                        .collect(Collectors.joining(","))
                    )
                    .map(errors -> String.join(",", errors, details.detail()))
                    .orElse(details.detail())
            )
            .flatMap(errorMessageDetail -> Mono.error(
                new UnableToProcessRequestException(errorMessageDetail)));
  }

  private static Function<ClientResponse, Mono<? extends Throwable>> get400ErrorMessage() {
    return response ->
        response
            .bodyToMono(ChesMailErrorResponse.class)
            .map(ChesMailErrorResponse::detail)
            .flatMap(errorMessageDetail -> Mono.error(
                new BadRequestException(errorMessageDetail)));
  }

  private String getToken() {

    try {

      OAuthClientRequest request =
          OAuthClientRequest
              .tokenLocation(configuration.getTokenUrl())
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
