package ca.bc.gov.app.controller.cognito;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.service.cognito.CognitoService;
import io.micrometer.observation.annotation.Observed;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The Cognito/Authorization controller.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Observed
public class CognitoController {

  public static final String LOCATION = "Location";
  private final CognitoService service;
  private final Environment environment;
  private final ForestClientConfiguration configuration;

  /**
   * Execute the login by redirecting to aws cognito.
   *
   * @param code           the provider value
   * @param serverResponse the server response to redirect to aws cognito
   * @return nothing, but force a redirect
   */
  @GetMapping("/login")
  @ResponseStatus(HttpStatus.FOUND)
  @SuppressWarnings("javasecurity:S5146")
  public Mono<Void> logon(
      @RequestParam(name = "code", required = false, defaultValue = "IDIR") String code,
      ServerHttpResponse serverResponse
  ) {
    if (
        Stream.of("idir", "bcsc", "bceidbusiness")
            .anyMatch(provider -> provider.equalsIgnoreCase(code))
    ) {

      String famUrl = String.format(
          "%s/oauth2/authorize"
          + "?client_id=%s"
          + "&response_type=code"
          + "&identity_provider=%s-%s"
          + "&scope=openid"
          + "&state=%s"
          + "&code_challenge=%s"
          + "&code_challenge_method=S256"
          + "&redirect_uri=%s",
          configuration.getCognito().getUrl(),
          configuration.getCognito().getClientId(),
          configuration.getCognito().getEnvironment(),
          code.toUpperCase(),
          UUID.randomUUID(),
          service.getCodeChallenge().orElse(""),
          configuration.getCognito().getRedirectUri()
      );

      serverResponse
          .getHeaders()
          .add(LOCATION, famUrl);
      log.info("Executing login for provider: {}", code);
      return Mono.empty();
    } else {
      log.error("Invalid provider code: {}", code);
      return Mono.error(new UnableToProcessRequestException("Invalid provider code."));
    }
  }

  /**
   * Execute the logout by redirecting to aws cognito.
   *
   * @param serverResponse the server response to redirect to aws cognito
   * @return nothing, but force a redirect
   */
  @GetMapping("/logout")
  @ResponseStatus(HttpStatus.FOUND)
  public Mono<Void> logout(
      ServerHttpResponse serverResponse
  ) {

    final String famUrl = String.format(
        "%s/logout"
        + "?client_id=%s"
        + "&response_type=code"
        + "&scope=openid"
        + "&redirect_uri=%s"
        + "&logout_uri=%s",
        configuration.getCognito().getUrl(),
        configuration.getCognito().getClientId(),
        configuration.getCognito().getRedirectUri(),
        configuration.getCognito().getLogoutUri()
    );

    serverResponse
        .addCookie(buildCookie(ApplicationConstant.ACCESS_TOKEN, StringUtils.EMPTY, -3600));
    serverResponse
        .addCookie(
            buildCookie(ApplicationConstant.ID_TOKEN, StringUtils.EMPTY, -3600));
    serverResponse
        .addCookie(buildCookie(ApplicationConstant.REFRESH_TOKEN, StringUtils.EMPTY, -3600));

    serverResponse
        .getHeaders()
        .add(LOCATION, famUrl);
    log.info("Executing logout");
    return Mono.empty();
  }

  @GetMapping("/refresh")
  @ResponseStatus(HttpStatus.FOUND)
  public Mono<Void> refresh(@RequestParam String code, ServerHttpResponse serverResponse) {
    log.info("Executing refresh for code: {}", code);
    return
        service
            .refreshToken(code)
            .map(authResponse -> {
              serverResponse
                  .addCookie(
                      buildCookie(ApplicationConstant.ACCESS_TOKEN, authResponse.accessToken(),
                          authResponse.expiresIn()));
              serverResponse
                  .addCookie(
                      buildCookie(ApplicationConstant.ID_TOKEN, authResponse.idToken(),
                          authResponse.expiresIn()));
              serverResponse
                  .addCookie(buildCookie(ApplicationConstant.REFRESH_TOKEN, code,
                      authResponse.expiresIn()));
              serverResponse
                  .setStatusCode(HttpStatus.FOUND);
              serverResponse
                  .getHeaders()
                  .add(LOCATION, configuration.getFrontend().getUrl());
              return serverResponse;
            })
            .then();
  }

  /**
   * Execute the callback by exchanging the authorization code for tokens.
   *
   * @param code           the authorization code
   * @param serverResponse the server response to redirect to the frontend
   * @return nothing, but force a redirect
   */
  @GetMapping("/callback")
  public Mono<ServerHttpResponse> extractToken(
      @RequestParam("code") String code,
      ServerHttpResponse serverResponse
  ) {
    log.info("Extracting JWT from code: {}", code);

    return
        Mono
            .just(code)
            .flatMap(service::exchangeAuthorizationCodeForTokens)
            .map(authResponse -> {
              serverResponse
                  .addCookie(
                      buildCookie(ApplicationConstant.ACCESS_TOKEN, authResponse.accessToken(),
                          authResponse.expiresIn()));
              serverResponse
                  .addCookie(
                      buildCookie(ApplicationConstant.ID_TOKEN, authResponse.idToken(),
                          authResponse.expiresIn()));
              serverResponse
                  .addCookie(
                      buildCookie(ApplicationConstant.REFRESH_TOKEN, authResponse.refreshToken(),
                          authResponse.expiresIn()));
              serverResponse
                  .setStatusCode(HttpStatus.FOUND);
              serverResponse
                  .getHeaders()
                  .add(LOCATION, configuration.getFrontend().getUrl());

              return serverResponse;
            });
  }

  private ResponseCookie buildCookie(
                          String cookieName, 
                          String cookieValue,
                          Integer expiresInSeconds) {
    return ResponseCookie
            .from(cookieName, cookieValue)
            .httpOnly(false)
            .sameSite(isLocal() ? "Lax" : "None")
            .path("/")
            .maxAge(Duration.ofSeconds(expiresInSeconds != null ? expiresInSeconds : 3600))
            .secure(!isLocal())
            .domain(configuration.getCognito().getCookieDomain())
            .build();
  }

  private boolean isLocal() {
    if (ArrayUtils.isEmpty(environment.getActiveProfiles())) {
      return false;
    }
    return Stream
        .of(environment.getActiveProfiles())
        .anyMatch(profile -> profile.startsWith("dev-"));
  }

}
