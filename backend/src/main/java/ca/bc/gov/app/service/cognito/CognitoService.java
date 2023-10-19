package ca.bc.gov.app.service.cognito;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.cognito.AuthResponseDto;
import ca.bc.gov.app.dto.cognito.RefreshRequestDto;
import ca.bc.gov.app.util.PkceUtil;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * CognitoService handles interactions with AWS Cognito.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CognitoService {

  private final ForestClientConfiguration configuration;
  private final WebClient webClient = WebClient
      .builder()
      .build();
  private final String codeVerify = PkceUtil.generateCodeVerifier();

  /**
   * Handles auth code exchange with Cognito.
   *
   * @return the {@link AuthResponseDto} with the tokens
   */
  public Mono<AuthResponseDto> exchangeAuthorizationCodeForTokens(String code) {

    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("grant_type", "authorization_code");
    requestBody.add("code", code);
    requestBody.add("client_id", configuration.getCognito().getClientId());
    requestBody.add("redirect_uri", configuration.getCognito().getRedirectUri());
    requestBody.add("code_verifier", codeVerify);

    return webClient
        .post()
        .uri(configuration.getCognito().getUrl() + "/oauth2/token")
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
        .cookie("XSRF-TOKEN", UUID.randomUUID().toString())
        .bodyValue(requestBody)
        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(AuthResponseDto.class));
  }

  public Mono<Void> refreshToken(String refreshToken) {
    return
        webClient
            .post()
            .uri(configuration.getCognito().getRefreshUrl())
            .header("Content-Type", "application/x-amz-json-1.1")
            .header("x-amz-target", "AWSCognitoIdentityProviderService.InitiateAuth")
            .header("x-amz-user-agent", "aws-amplify/5.0.4 auth framework/4")
            .bodyValue(new RefreshRequestDto(
                    configuration.getCognito().getClientId(),
                    "REFRESH_TOKEN_AUTH",
                    Map.of(
                        "REFRESH_TOKEN", refreshToken,
                        "DEVICE_KEY", null
                    )
                )
            )
            .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Objects.class))
            .then();
  }

  /**
   * Gets a code challenge for Pkce.
   *
   * @return the code challenge value optional or empty if an error occurs.
   */
  public Optional<String> getCodeChallenge() {
    try {
      return Optional.of(PkceUtil.generateCodeChallenge(codeVerify));
    } catch (NoSuchAlgorithmException e) {
      log.error("Cannot generate code challenge", e);
    }
    return Optional.empty();
  }

}
