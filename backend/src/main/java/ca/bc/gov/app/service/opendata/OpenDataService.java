package ca.bc.gov.app.service.opendata;

import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.opendata.Feature;
import ca.bc.gov.app.dto.opendata.OpenData;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class OpenDataService {

  private final BcMapsService bcMapsService;
  private final SacService sacService;
  private final ProvinceCodeRepository provinceCodeRepository;

  public Flux<ClientDetailsDto> getFirstNationData(String nationName) {
    return bcMapsService
        .getFirstNationData(nationName)
        .switchIfEmpty(sacService.getFirstNationData(nationName))
        .flatMapIterable(OpenData::features)
        .doOnNext(openDataFeature ->
            log.info("Returning first nation data for nationName: {} {}", nationName,
                openDataFeature)
        )
        .flatMap(this::convertToDto);
  }

  private Mono<ClientDetailsDto> convertToDto(Feature openDataFeature) {
    return loadProvince("CA", openDataFeature.properties().officeProvince())
        .map(provinceDto -> {
            ClientAddressDto addressDto = new ClientAddressDto(
                StringUtils.defaultIfBlank(
                    openDataFeature.properties().addressLine2(),
                    openDataFeature.properties().addressLine1()
                ),
                StringUtils.EMPTY,
                null,
                new ClientValueTextDto("CA", "Canada"),
                provinceDto,
                openDataFeature.properties().officeCity(),
                StringUtils.defaultIfBlank(
                    openDataFeature.properties().officePostalCode(), 
                    StringUtils.EMPTY)
                  .replace(" ", StringUtils.EMPTY),
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                0,
                "Mailing Address"
            );

            return new ClientDetailsDto(
                openDataFeature.properties().getNationName(),
                openDataFeature.properties().getNationId() + "",
                true,
                addressDto.isValid() ? List.of(addressDto) : List.of(),
                List.of()
            );
        });
  }
  
  private Mono<ClientValueTextDto> loadProvince(String countryCode, String province) {
    return
        provinceCodeRepository
            .findByCountryCodeAndProvinceCode(countryCode, province)
            .map(
                entity -> new ClientValueTextDto(entity.getProvinceCode(), entity.getDescription())
            )
            .defaultIfEmpty(new ClientValueTextDto(province, province));
  }

}
