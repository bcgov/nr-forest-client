package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.service.client.matches.StepMatcher;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientMatchService {

  private final List<StepMatcher> stepMatchers;
  private final Predicate<ClientSubmissionDto> isMatcherEnabled;

  /**
   * This method is responsible for matching clients based on the provided ClientSubmissionDto and
   * the step number. It first validates the input data and throws an InvalidRequestObjectException if
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

    if (!isMatcherEnabled.test(dto)) {
      return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, StringUtils.EMPTY));
    }

    if (dto == null) {
      return Mono.error(new InvalidRequestObjectException("Invalid data"));
    }

    if (dto.businessInformation() == null) {
      return Mono.error(new InvalidRequestObjectException("Invalid business information"));
    }

    if (StringUtils.isBlank(dto.businessInformation().clientType())) {
      return Mono.error(new InvalidRequestObjectException("Invalid client type"));
    }

    if (dto.location() == null) {
      return Mono.error(new InvalidRequestObjectException("Invalid location"));
    }

    return switch (step) {
      case 1 -> matchStep1(dto);
      case 2 -> findAndRunMatcher(dto, StepMatchEnum.STEP2);
      case 3 -> findAndRunMatcher(dto, StepMatchEnum.STEP3);
      default -> Mono.error(new InvalidRequestObjectException("Invalid step"));
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
      case "RSP" -> {
        return findAndRunMatcher(dto, StepMatchEnum.STEP1REGISTERED);
      }
      case "R" -> {
        return findAndRunMatcher(dto, StepMatchEnum.STEP1FIRSTNATION);
      }
      case "G" -> {
        return findAndRunMatcher(dto, StepMatchEnum.STEP1GOVERNMENT);
      }
      case "I" -> {
        return findAndRunMatcher(dto, StepMatchEnum.STEP1INDIVIDUAL);
      }
      case "F" -> {
        return findAndRunMatcher(dto, StepMatchEnum.STEP1FORESTS);
      }
      case "U" -> {
        return findAndRunMatcher(dto, StepMatchEnum.STEP1UNREGISTERED);
      }
      default -> {
        return Mono.error(new InvalidRequestObjectException("Invalid client type"));
      }
    }
  }

  /**
   * This method finds and runs the appropriate matcher based on the required step. If no matcher is
   * found, it returns a Mono error with a NotImplementedException.
   *
   * @param dto          The ClientSubmissionDto object containing the client data to be matched.
   * @param requiredStep The required step for which to find and run the matcher.
   * @return A Mono<Void> indicating when the matching process is complete. If no matcher is found,
   * a Mono error is returned.
   */
  private Mono<Void> findAndRunMatcher(
      ClientSubmissionDto dto,
      StepMatchEnum requiredStep
  ) {
    return Flux
        .fromIterable(stepMatchers)
        .filter(matcher -> requiredStep.equals(matcher.getStepMatcher()))
        .switchIfEmpty(Mono.error(new NotImplementedException("No matcher found")))
        .flatMap(matcher -> matcher.matchStep(dto))
        .next();

  }

}
