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
   * This is a utility class that provides methods for handling reactive streams retry. It is used
   * to control the retry behavior of reactive streams, particularly in the context of an empty
   * result, when an audit entry is required but it wasn't created yet by the table trigger. Returns
   * a function that, when applied to a {@link Flux} of {@link Long}, will:
   * <ul>
   *   <li>Zip the input flux with a range of 1 to 5, effectively limiting retries to 5 attempts.</li>
   *   <li>Delay each element by 200 milliseconds.</li>
   *   <li>Log a warning message on each retry, including the retry count and the provided reason.</li>
   * </ul>
   *
   * @param reason the reason for the audit entry, included in the log message
   * @return a function that transforms a {@link Flux} of {@link Long} into a {@link Publisher} with
   * logging and delay
   */
  public static Function<Flux<Long>, Publisher<?>> repeatAndLog(String reason) {
    return repeat ->
        repeat
            .zipWith(Flux.range(1, 5))
            .delayElements(Duration.ofMillis(200))
            .doOnNext(tuple -> log.warn(
                    "[Check #{}] Waiting for trigger to create the reason audit entry for {} change",
                    tuple.getT2(),
                    reason
                )
            );
  }

}
