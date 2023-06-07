package ca.bc.gov.app.service.ches;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
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
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.exception.InvalidRoleException;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.exception.UnexpectedErrorException;
import ca.bc.gov.app.util.ValidationUtil;
import ca.bc.gov.app.validator.ches.ChesRequestValidator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
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

  private final ForestClientConfiguration configuration;
  private final ChesRequestValidator validator;
  private final WebClient webClient;
  private final Configuration freeMarkerConfiguration;

  public ChesCommonServicesService(
      ForestClientConfiguration configuration,
      @Qualifier("chesApi") WebClient webClient,
      ChesRequestValidator validator
  ) {
    this.configuration = configuration;
    this.webClient = webClient;
    this.validator = validator;
    this.freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_31);
    freeMarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/templates");
  }

  @Setter
  private OAuthClient oauthClient = new OAuthClient(new URLConnectionClient());

  /**
   * Sends an email using the BC Government's Common Email Service (Ches)
   * via HTTP POST request using WebClient.
   *
   * @param requestContent the {@link ChesRequest} object representing the email to be sent
   * @return a {@link Mono} that the transaction ID of the email send operation upon completion
   * @throws InvalidAccessTokenException if the authorization token is invalid or expired
   * @throws InvalidRoleException        if does not have the required role to perform the requested action
   */
  public Mono<String> sendEmail(ChesRequest requestContent) {

    if (requestContent == null) {
      return Mono.error(new InvalidRequestObjectException("no request body was provided"));
    }

    return
        ValidationUtil
            .validateReactive(requestContent, ChesRequest.class, validator)
            .map(request ->
                new ChesMailRequest(
                    null,
                    null,
                    ChesMailBodyType.HTML,
                    request.emailBody(),
                    null,
                    0,
                    ChesMailEncoding.UTF_8,
                    "FSA_donotreply@gov.bc.ca",
                    ChesMailPriority.NORMAL,
                    "Forest Client Application Confirmation",
                    null,
                    request.emailTo()
                )
            )
            .flatMap(request ->
                webClient
                    .post()
                    //.uri(configuration.getChes().getUri())
                    .uri("https://ches.api.gov.bc.ca/api/v1/email")
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
            )
            .map(response -> response.txId().toString())
            .doOnError(error -> log.error("Failed to send email", error));

  }

  /**
   * Builds a String representation of an HTML email template by applying a map of variables
   * to the FreeMarker template identified by the given template name.
   *
   * @param templateName the name of the FreeMarker template, without the ".html" extension
   * @param variables    a map of variable names and their corresponding values to be used
   *                     when processing the template
   * @return a Mono that emits the String representation of the processed template, or
   *         an error if an exception occurs during template processing
   */
  public Mono<String> buildTemplate(String templateName, Map<String, Object> variables) {
    StringWriter writer = new StringWriter();
    try {
      Template template = freeMarkerConfiguration.getTemplate(templateName + ".html");
      template.process(variables, writer);
    } catch (TemplateException | IOException e) {
      return Mono.error(e);
    }

    return Mono.just(writer.toString());
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

      System.out.println("Client ID = " + configuration.getChes().getClientId());
      /*OAuthClientRequest request =
          OAuthClientRequest
              .tokenLocation(configuration.getChes().getTokenUrl())
              .setGrantType(GrantType.CLIENT_CREDENTIALS)
              .setClientId(configuration.getChes().getClientId())
              .setClientSecret(configuration.getChes().getClientSecret())
              .setScope(configuration.getChes().getScope())
              .buildBodyMessage();*/
      
      OAuthClientRequest request =
          OAuthClientRequest
              .tokenLocation("https://loginproxy.gov.bc.ca/auth/realms/comsvcauth/protocol/openid-connect/token")
              .setGrantType(GrantType.CLIENT_CREDENTIALS)
              .setClientId("0943A91E-26163FF5D3A")
              .setClientSecret("cbND0fx0ODWKXlYzn52mUt10RRO1ju3s")
              .setScope(configuration.getChes().getScope())
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
