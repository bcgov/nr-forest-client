package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides country and province lookup operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCountryProvinceService {

  private final CountryCodeRepository countryCodeRepository;
  private final ProvinceCodeRepository provinceCodeRepository;

  /**
   * Lists active countries for the requested page.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listCountries(
      int page,
      int size,
      LocalDate currentDate
  ) {
    log.info("Loading countries for page {} with size {}", page, size);
    return countryCodeRepository
        .findAllBy(PageRequest.of(page, size, Sort.by("order", "description")))
        .filter(entity -> isActiveOn(currentDate, entity.getEffectiveAt(), entity.getExpiredAt()))
        .map(entity -> new CodeNameDto(entity.getCountryCode(), entity.getDescription()));
  }

  /**
   * Lists provinces or states for the requested country.
   *
   * @param countryCode The code of the country to list provinces from.
   * @param page        The page number, it is a 0-index base.
   * @param size        The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listProvinces(String countryCode, int page, int size) {
    log.info("Loading provinces for {} with page {} and size {}", countryCode, page, size);
    return provinceCodeRepository
        .findByCountryCode(countryCode, PageRequest.of(page, size, Sort.by("description")))
        .map(entity -> new CodeNameDto(entity.getProvinceCode(), entity.getDescription()));
  }

  /**
   * Retrieves a country by code.
   *
   * @param countryCode The code of the country to retrieve information for.
   * @return A Mono that emits the matching {@code CodeNameDto}, or an empty result.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getCountryByCode(String countryCode) {
    log.info("Loading country for {}", countryCode);
    return countryCodeRepository
        .findByCountryCode(countryCode)
        .map(entity -> new CodeNameDto(entity.getCountryCode(), entity.getDescription()));
  }

  /**
   * Loads a country value by description.
   *
   * @param countryCode The description of the country to retrieve information for.
   * @return A Mono that emits the matching {@code ClientValueTextDto}, or a default value when no
   *     match is found.
   * @see ClientValueTextDto
   */
  public Mono<ClientValueTextDto> loadCountry(String countryCode) {
    return countryCodeRepository
        .findByDescription(countryCode)
        .map(entity -> new ClientValueTextDto(entity.getCountryCode(), entity.getDescription()))
        .defaultIfEmpty(new ClientValueTextDto(StringUtils.EMPTY, countryCode));
  }

  /**
   * Retrieves a province by country code and province code.
   *
   * @param countryCode  The code of the country to retrieve the province from.
   * @param provinceCode The code of the province to retrieve information for.
   * @return A Mono that emits the matching {@code CodeNameDto}, or an empty result.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getProvinceByCountryAndProvinceCode(
      String countryCode,
      String provinceCode
  ) {
    log.info(
        "Loading province by country and province code {} {}",
        countryCode,
        provinceCode
    );
    return provinceCodeRepository
        .findByCountryCodeAndProvinceCode(countryCode, provinceCode)
        .map(entity -> new CodeNameDto(entity.getProvinceCode(), entity.getDescription()));
  }

  /**
   * Loads a province value by country and province code.
   *
   * @param countryCode The code of the country to retrieve the province from.
   * @param provinceCode The code of the province to retrieve information for.
   * @return A Mono that emits the matching {@code ClientValueTextDto}, or a default value when no
   *     match is found.
   * @see ClientValueTextDto
   */
  public Mono<ClientValueTextDto> loadProvince(String countryCode, String provinceCode) {
    return provinceCodeRepository
        .findByCountryCodeAndProvinceCode(countryCode, provinceCode)
        .map(entity -> new ClientValueTextDto(entity.getProvinceCode(), entity.getDescription()))
        .defaultIfEmpty(new ClientValueTextDto(provinceCode, provinceCode));
  }

  private boolean isActiveOn(LocalDate currentDate, LocalDate effectiveAt, LocalDate expiredAt) {
    boolean isBeforeExpiry = currentDate.isBefore(expiredAt) || currentDate.isEqual(expiredAt);
    boolean isAfterEffective = currentDate.isAfter(effectiveAt)
        || currentDate.isEqual(effectiveAt);
    return isBeforeExpiry && isAfterEffective;
  }

}
