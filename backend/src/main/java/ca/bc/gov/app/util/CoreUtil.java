package ca.bc.gov.app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreUtil {

  /**
   * Encodes a string using UTF-8 encoding.
   *
   * @param clientName the string to encode
   * @return the encoded string
   */
  public static String encodeString(String clientName) {
    try {
      return URLEncoder.encode(clientName, "utf-8");
    } catch (UnsupportedEncodingException e) {
      // This should never happen since utf-8 is a valid encoding, but if it does,
      // return the original string
      return clientName;
    }
  }

}
