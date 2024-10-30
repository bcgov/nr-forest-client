package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.processor.ProcessorService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/processor", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ProcessController {

  private final ProcessorService processorService;

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> processStaffSubmission(@PathVariable Integer id) {
    log.info("Processing staff submission with id {}", id);
    return
        processorService
            .processedMessage(id, ValidationSourceEnum.STAFF)
            .doOnNext(submission -> log.info("Submission {} was processed", submission))
            .then();
  }

}
