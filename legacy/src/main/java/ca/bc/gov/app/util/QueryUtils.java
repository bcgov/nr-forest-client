package ca.bc.gov.app.util;

import java.time.Duration;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryUtils {

  /**
   * Utility method for retrying a reactive stream when an audit entry has not yet been created.
   *
   * <p>When applied to a {@link Flux} of {@link Long}, this function:
   *
   * <ul>
   *   <li>Zips the input flux with a range of 1 to 5, limiting retries to 5 attempts.</li>
   *   <li>Delays each element by 200 milliseconds.</li>
   *   <li>Logs a warning on each retry with the retry count and reason.</li>
   * </ul>
   *
   * @param reason the reason for the audit entry, included in the log message
   * @return a function that transforms a {@link Flux} of {@link Long} into a
   *     {@link Publisher} with logging and delay
   */
  public static Function<Flux<Long>, Publisher<?>> repeatAndLog(String reason) {
    return repeat ->
        repeat
            .zipWith(Flux.range(1, 5))
            .delayElements(Duration.ofMillis(200))
            .doOnNext(tuple ->
                log.warn(
                    "[Check #{}] Waiting for trigger to create the reason audit entry "
                        + "for {} change",
                    tuple.getT2(),
                    reason
                ));
  }

}
