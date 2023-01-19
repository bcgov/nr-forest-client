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

  /**
   * <p><b>Log Content</b></p>
   * Log the content of the future and add a log message as well.
   *
   * @param logMessage A string message to be logged
   * @param <T>        The type that will be passed through the future.
   * @return the same content of the future
   */
  public static <T> Consumer<T> logContent(Logger log, String logMessage) {
    return received -> log.info(String.format("%s {}", logMessage), received);
  }

  /**
   * <p><b>Log Content</b></p>
   * Log the content of the future and add a log message as well.
   *
   * @param logMessage A string message to be logged
   * @param params     the params array to be passed to SL4J
   * @param <T>        The type that will be passed through the future.
   * @return the same content of the future
   */
  public static <T> Consumer<T> logContent(Logger log, String logMessage, Object... params) {
    return received -> log.info(String.format("%s {}", logMessage), params, received);
  }

  /**
   * <p><b>Log Content</b></p>
   *
   * @param log        The logger
   * @param logMessage A string message to be logged
   * @param params     the params array to be passed to SL4J
   * @param <T>        The type that will be passed through the future.
   * @return the same content of the future
   */
  public static <T> Consumer<T> logMessage(Logger log, String logMessage, Object... params) {
    return received -> log.info(logMessage, params);
  }
}
