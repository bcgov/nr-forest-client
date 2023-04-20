package ca.bc.gov.app.util;

import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonoUtil {
  /**
   * <p><b>Log Content</b></p>
   * Log the content of the future.
   *
   * @param <T> The type that will be passed through the future.
   * @return the same content of the future
   */
  public static <T> Consumer<T> logContent(Logger log) {
    return received -> log.info("{}", received);
  }

}
