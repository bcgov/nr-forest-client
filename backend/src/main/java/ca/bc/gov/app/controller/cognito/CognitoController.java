package ca.bc.gov.app.controller.cognito;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.service.cognito.CognitoService;
import java.net.URI;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class CognitoController {

  private final CognitoService service;
  private final Environment environment;
  private final ForestClientConfiguration configuration;

  @GetMapping("/login")
  @ResponseStatus(HttpStatus.FOUND)
  public Mono<Void> logon(
      @RequestParam(name = "code", required = false, defaultValue = "IDIR") String code,
      ServerHttpResponse serverResponse
  ) {

    String famURL = String.format(
        "%s/oauth2/authorize?client_id=%s&response_type=code&identity_provider=%s-%s&scope=openid&state=%s&code_challenge=%s&code_challenge_method=S256&redirect_uri=%s",
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
        .add("Location", famURL);

    return Mono.empty();
  }

  @GetMapping("/logout")
  @ResponseStatus(HttpStatus.FOUND)
  public Mono<Void> logout(
      ServerHttpResponse serverResponse
  ) {

    String famURL = String.format(
        "%s/logout?client_id=%s&response_type=code&scope=openid&redirect_uri=%s&logout_uri=%s",
        configuration.getCognito().getUrl(),
        configuration.getCognito().getClientId(),
        configuration.getCognito().getLogoutUri(),
        configuration.getCognito().getLogoutUri()
    );

    serverResponse
        .addCookie(buildCookie("accessToken", StringUtils.EMPTY,-3600));
    serverResponse
        .addCookie(
            buildCookie("idToken", StringUtils.EMPTY, -3600));
    serverResponse
        .addCookie(buildCookie("refreshToken", StringUtils.EMPTY,-3600));

    serverResponse
        .getHeaders()
        .add("Location", famURL);

    return Mono.empty();
  }

  @GetMapping("/callback")
  public Mono<ServerHttpResponse> extractToken(
      @RequestParam("code") String code,
      ServerHttpResponse serverResponse
  ) {

    return
        Mono
            .just(code)
            .flatMap(service::exchangeAuthorizationCodeForTokens)
            .map(authResponse -> {
              serverResponse
                  .addCookie(buildCookie("accessToken", authResponse.accessToken(),
                      authResponse.expiresIn()));
              serverResponse
                  .addCookie(
                      buildCookie("idToken", authResponse.idToken(), authResponse.expiresIn()));
              serverResponse
                  .addCookie(buildCookie("refreshToken", authResponse.refreshToken(),
                      authResponse.expiresIn()));
              serverResponse
                  .setStatusCode(HttpStatus.FOUND);
              serverResponse
                  .getHeaders()
                  .add("Location", configuration.getFrontend().getUrl());

              return serverResponse;
            });
  }


  private ResponseCookie buildCookie(String cookieName, String cookieValue, int expiresInSeconds) {
    return ResponseCookie.from(cookieName, cookieValue)
        .httpOnly(!isLocal())
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ofSeconds(expiresInSeconds))
        .secure(!isLocal())
        .domain(URI.create(configuration.getFrontend().getUrl()).getHost())
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
