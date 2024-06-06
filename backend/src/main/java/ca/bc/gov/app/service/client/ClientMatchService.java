package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.DataMatchException;
import io.micrometer.observation.annotation.Observed;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientMatchService {

  private final ClientLegacyService legacyService;

  /**
   * This method is responsible for matching clients based on the provided ClientSubmissionDto and
   * the step number. It first validates the input data and throws an IllegalArgumentException if
   * any of the data is invalid. Then, it delegates the matching process to the appropriate method
   * based on the step number.
   *
   * @param dto  The ClientSubmissionDto object containing the client data to be matched.
   * @param step The step number indicating which matching method to use.
   * @return A Mono of a Map containing the matching results. If no match is found, an empty map is
   * returned.
   * @throws IllegalArgumentException if the input data is invalid or if the step number is not 1,
   *                                  2, or 3.
   */
  public Mono<Void> matchClients(
      ClientSubmissionDto dto,
      int step
  ) {

    if (dto == null) {
      return Mono.error(new IllegalArgumentException("Invalid data"));
    }
    if (dto.businessInformation() == null) {
      return Mono.error(new IllegalArgumentException("Invalid business information"));
    }
    if (StringUtils.isBlank(dto.businessInformation().clientType())) {
      return Mono.error(new IllegalArgumentException("Invalid client type"));
    }
    if (dto.location() == null) {
      return Mono.error(new IllegalArgumentException("Invalid location"));
    }

    return switch (step) {
      case 1 -> matchStep1(dto);
      case 2 -> matchStep2(dto);
      case 3 -> matchStep3(dto);
      default -> Mono.error(new IllegalArgumentException("Invalid step"));
    };

  }

  /**
   * This method does the fuzzy match for the client first step, Business Information. This will
   * delegate to specific method that will use the correct matches for each one of the types
   * selected.
   *
   * @param dto The provided data filled by the user
   * @return An empty map if no match found, or a map with matches (usually in form of exception)
   */
  private Mono<Void> matchStep1(ClientSubmissionDto dto) {

    switch (dto.businessInformation().clientType()) {
      case "BCR" -> {
        return Mono.error(new NotImplementedException("Client type match not implemented yet"));
      }
      case "R" -> {
        return Mono.error(new NotImplementedException("Client type match not implemented yet"));
      }
      case "G" -> {
        return Mono.error(new NotImplementedException("Client type match not implemented yet"));
      }
      case "I" -> {
        return matchIndividual(dto);
      }
      case "F" -> {
        return Mono.error(new NotImplementedException("Client type match not implemented yet"));
      }
      case "U" -> {
        return Mono.error(new NotImplementedException("Client type match not implemented yet"));
      }
      default -> {
        return Mono.error(new IllegalArgumentException("Invalid client type"));
      }
    }
  }

  private Mono<Void> matchIndividual(ClientSubmissionDto dto) {

    // Search for individual without document id
    Mono<MatchResult> individualFuzzyMatch =
        //Do the search
        legacyService
            .searchIndividual(
                dto.businessInformation().firstName(),
                dto.businessInformation().businessName(),
                dto.businessInformation().birthdate(),
                null
            )
            .map(ForestClientDto::clientNumber)
            .collectList()
            .map(clientNumbers -> String.join(",", clientNumbers))
            .map(clientNumbers -> new MatchResult("businessInformation.businessName", clientNumbers,
                true));

    // Search for individual with document id
    Mono<MatchResult> individualFullMatch =
        legacyService
            .searchIndividual(
                dto.businessInformation().firstName(),
                dto.businessInformation().businessName(),
                dto.businessInformation().birthdate(),
                dto.businessInformation().idValue()
            )
            .map(ForestClientDto::clientNumber)
            .collectList()
            .map(clientNumbers -> String.join(",", clientNumbers))
            .map(clientNumbers -> new MatchResult("businessInformation.businessName", clientNumbers,
                false));

    // Search for document itself
    Mono<MatchResult> documentFullMatch =
        legacyService.searchDocument(dto.businessInformation().idType(),
                dto.businessInformation().idValue()
            )
            .map(ForestClientDto::clientNumber)
            .collectList()
            .map(clientNumbers -> String.join(",", clientNumbers))
            .map(clientNumbers -> new MatchResult("businessInformation.identification",
                clientNumbers, false));

    return
        Flux
            .concat(
                individualFuzzyMatch,
                individualFullMatch,
                documentFullMatch
            )
            .sort(Comparator.comparing(MatchResult::fuzzy))
            .distinct(MatchResult::field)
            .collectList()
            .map(DataMatchException::new)
            .flatMap(Mono::error);

  }

  /**
   * This method does the fuzzy match for the client second step, Address.
   *
   * @param dto The provided data filled by the user
   * @return An empty map if no match found, or a map with matches (usually in form of exception)
   */
  private Mono<Void> matchStep2(ClientSubmissionDto dto) {
    log.warn("Step 2 not implemented yet");
    return Mono.empty();
  }

  /**
   * This method does the fuzzy match for the client third step, Contacts.
   *
   * @param dto The provided data filled by the user
   * @return An empty map if no match found, or a map with matches (usually in form of exception)
   */
  private Mono<Void> matchStep3(ClientSubmissionDto dto) {
    log.warn("Step 3 not implemented yet");
    return Mono.empty();
  }

}
