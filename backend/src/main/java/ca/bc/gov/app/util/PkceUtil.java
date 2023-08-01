package ca.bc.gov.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for generating PKCE code verifier and code challenge.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PkceUtil {

  /**
   * Generates a random string of 43 characters (256 bits) for use as a code verifier.
   * @return a random string of 43 characters (256 bits)
   */
  public static String generateCodeVerifier() {
    SecureRandom random = new SecureRandom();
    byte[] codeVerifierBytes = new byte[32]; // 256 bits (43 characters after base64 encoding)
    random.nextBytes(codeVerifierBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
  }

  /**
   * Generates a code challenge from the given code verifier.
   * @param codeVerifier the code verifier to generate a code challenge from
   * @return the code challenge
   * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available
   */
  public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] codeChallengeBytes = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(codeChallengeBytes);
  }

}
