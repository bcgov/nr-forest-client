package ca.bc.gov.app.service.cognito;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("Unit Test | Cognito Service")
class CognitoServiceTest {

  private final CognitoService service = new CognitoService(
      null,
      WebClient.builder().build()
  );

  @Test
  @DisplayName("should get a code challenge")
  void shouldGetCodeChallenge() {
    assertThat(service.getCodeChallenge())
        .isNotNull()
        .isNotEmpty()
        .isPresent()
        .hasValueSatisfying(str -> assertThat(str)
            .isNotNull()
            .isNotEmpty()
            .hasSize(43)
        );
  }

}