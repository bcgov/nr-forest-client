package ca.bc.gov.app.service.opendata;

import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.opendata.Feature;
import ca.bc.gov.app.dto.opendata.OpenData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Slf4j
@Service
public class OpenDataService {

  private final BcMapsService bcMapsService;
  private final SacService sacService;

  public Flux<ClientDetailsDto> getFirstNationData(String nationName) {
    return bcMapsService
        .getFirstNationData(nationName)
        .switchIfEmpty(sacService.getFirstNationData(nationName))
        .flatMapIterable(OpenData::features)
        .doOnNext(openDataFeature ->
            log.info("Returning first nation data for nationName: {} {}", nationName,
                openDataFeature)
        )
        .map(this::convertToDto);
  }

  private ClientDetailsDto convertToDto(Feature openDataFeature) {
    return new ClientDetailsDto(
        openDataFeature.properties().getNationName(),
        openDataFeature.properties().getNationId() + "",
        true,
        List.of(
            new ClientAddressDto(
                openDataFeature.properties().addressLine1(),
                new ClientValueTextDto("CA", "Canada"),
                new ClientValueTextDto(openDataFeature.properties().officeProvince(), null),
                openDataFeature.properties().officeCity(),
                StringUtils.defaultIfBlank(openDataFeature.properties().officePostalCode(),
                    StringUtils.EMPTY).replace(" ", StringUtils.EMPTY),
                0,
                StringUtils.defaultIfBlank(
                    openDataFeature.properties().addressLine2(),
                    openDataFeature.properties().siteName()
                )
            )
        ),
        List.of()
    );
  }

}
