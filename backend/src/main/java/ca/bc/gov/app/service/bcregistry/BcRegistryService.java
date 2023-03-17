package ca.bc.gov.app.service.bcregistry;

import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryIdentificationDto;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j

public class BcRegistryService {

  private final WebClient bcRegistryApi;

  public BcRegistryService(@Qualifier("bcRegistryApi") WebClient bcRegistryApi) {
    this.bcRegistryApi = bcRegistryApi;
  }


  public Mono<BcRegistryBusinessDto> getCompanyStanding(String clientNumber) {
    return
        bcRegistryApi
            .get()
            .uri("/business/api/v2/businesses/{clientNumber}",
                Map.of("clientNumber", clientNumber)
            )
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(404)),
                exception -> Mono.error(new NoClientDataFound(clientNumber))
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .bodyToMono(BcRegistryIdentificationDto.class)
            .map(BcRegistryIdentificationDto::business);

  }

  public Mono<BcRegistryBusinessAdressesDto> getAddresses(String clientNumber) {
    return
        bcRegistryApi
            .get()
            .uri("/business/api/v2/businesses/{clientNumber}/addresses",
                Map.of("clientNumber", clientNumber))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(404)),
                exception -> Mono.error(new NoClientDataFound(clientNumber))
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .bodyToMono(BcRegistryIdentificationDto.class)
            .map(BcRegistryIdentificationDto::businessOffice);
  }

}
