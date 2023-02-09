package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientCodeTypeDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ClientService {
  private final ClientTypeCodeRepository clientTypeCodeRepository;
  private final CountryCodeRepository countryCodeRepository;
  private final ProvinceCodeRepository provinceCodeRepository;

  public Flux<ClientCodeTypeDto> findActiveClientTypeCodes(LocalDate targetDate) {

    return
        clientTypeCodeRepository
            .findActiveAt(targetDate)
            .map(entity -> new ClientCodeTypeDto(
                entity.getCode(),
                entity.getDescription(),
                entity.getEffectiveAt(),
                entity.getExpiredAt(),
                null)
            );
  }

  /**
   * <p><b>List countries</b></p>
   * List countries by page with a defined size. The list will be sorted by order and country name.
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link ClientNameCodeDto} entries.
   */
  public Flux<ClientNameCodeDto> listCountries(int page, int size) {
    return countryCodeRepository
        .findBy(PageRequest.of(page, size, Sort.by("order","description")))
        .map(entity -> new ClientNameCodeDto(entity.getCountryCode(), entity.getDescription()));
  }

  /**
   * <p><b>List Provinces</b></p>
   * List provinces by country (which include states) by page with a defined size.
   * The list will be sorted by province name.
   * @param countryCode The code of the country to list provinces from.
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link ClientNameCodeDto} entries.
   */
  public Flux<ClientNameCodeDto> listProvinces(String countryCode, int page, int size){
    return provinceCodeRepository
        .findByCountryCode(countryCode,PageRequest.of(page, size, Sort.by("description")))
        .map(entity -> new ClientNameCodeDto(entity.getProvinceCode(), entity.getDescription()));
  }


}
