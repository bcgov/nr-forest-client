package ca.bc.gov.app.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class HealthExchangeFilterFunction implements ExchangeFilterFunction {

  private final ManualHealthIndicator healthIndicator;

  @Override
  public Mono<ClientResponse> filter(
      ClientRequest request,
      ExchangeFunction next
  ) {
    return next.exchange(request)
        .doOnNext(clientResponse -> healthIndicator.isHealthy(
            clientResponse.statusCode().is2xxSuccessful(), null)
        )
        .doOnError(throwable -> healthIndicator.isHealthy(false, throwable));
  }

}
