package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.repository.client.DistrictCodeRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientDistrictService {

  private final DistrictCodeRepository districtCodeRepository;

  /**
   * <p><b>List natural resource districts</b></p>
   * <p>List natural resource districts by page with a defined size.</p>
   * List natural resource districts by page with a defined size. The list will be sorted by
   * district name.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @param currentDate The date to be used as reference.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> getActiveDistrictCodes(
      int page,
      int size,
      LocalDate currentDate
      ) {
    log.info("Loading natural resource districts for page {} with size {}", page, size);
    return districtCodeRepository
        .findAllBy(PageRequest.of(page, size, Sort.by("description")))
        .filter(entity -> (currentDate.isBefore(entity.getExpiredAt())
            || currentDate.isEqual(entity.getExpiredAt()))
            &&
            (currentDate.isAfter(entity.getEffectiveAt())
                || currentDate.isEqual(entity.getEffectiveAt())))
        .map(entity -> new CodeNameDto(entity.getCode(), entity.getDescription()));
  }


  /**
   * Retrieves natural resource district information by its district code. This method queries the
   * {@code districtCodeRepository} to find a district entity with the specified district code. If a
   * matching entity is found, it is mapped to a {@code DistrictDto} object, which encapsulates the
   * district code, description and email. The resulting data is wrapped in a Mono, which represents
   * the asynchronous result of the operation.
   *
   * @param districtCode The code of the district to retrieve information for.
   * @return A Mono that emits the {@code DistrictDto} object if a matching district is found, or an
   * empty result if no match is found.
   * @see DistrictDto
   */
  public Mono<DistrictDto> getDistrictByCode(String districtCode) {
    log.info("Loading district for {}", districtCode);
    return districtCodeRepository
        .findByCode(districtCode)
        .map(entity -> new DistrictDto(
                entity.getCode(),
                entity.getDescription(),
                entity.getEmailAddress()
            )
        );
  }

  /**
 * Retrieves the full description of a natural resource district by its district code.
 * This method queries the {@code districtCodeRepository} to find a district entity with the specified district code.
 * If a matching entity is found, it constructs a string combining the district code and description.
 * If no matching entity is found, an empty string is returned.
 *
 * @param districtCode The code of the district to retrieve the full description for.
 * @return A Mono that emits the full description string if a matching district is found, or an empty string if no match is found.
 */
public Mono<String> getDistrictFullDescByCode(String districtCode) {
  return Mono.justOrEmpty(districtCode)
      .flatMap(districtCodeRepository::findByCode)
      .map(districtCodeEntity -> districtCodeEntity.getCode() + " - "
          + districtCodeEntity.getDescription())
      .defaultIfEmpty("");
}


}
