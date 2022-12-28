package ca.bc.gov.app.util;

import java.net.URLEncoder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreUtil {

  public static String encodeString(String clientName) {
    try {
      return URLEncoder.encode(clientName, "utf-8");
    } catch (Exception e) {
      return clientName;
    }
  }

}
