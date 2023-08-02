package ca.bc.gov.app.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test : PkceUtil")
class PkceUtilTest {

  @Test
  @DisplayName("should generate code verifier")
  void shouldGenerateCodeVerifier() {
    assertThat(PkceUtil.generateCodeVerifier())
        .isNotNull()
        .isNotEmpty()
        .hasSize(43);
  }

  @Test
  @DisplayName("should generate code challenge")
  void shouldGenerateCodeChallenge() throws NoSuchAlgorithmException {
    assertThat(PkceUtil.generateCodeChallenge(PkceUtil.generateCodeVerifier()))
        .isNotNull()
        .isNotEmpty()
        .hasSize(43);
  }

}