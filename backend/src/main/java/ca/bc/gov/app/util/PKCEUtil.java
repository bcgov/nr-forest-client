package ca.bc.gov.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PKCEUtil {

  public static String generateCodeVerifier() {
    SecureRandom random = new SecureRandom();
    byte[] codeVerifierBytes = new byte[32]; // 256 bits (43 characters after base64 encoding)
    random.nextBytes(codeVerifierBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
  }

  public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] codeChallengeBytes = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(codeChallengeBytes);
  }

}
