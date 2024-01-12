package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class LegacyPersistenceService {

  private final List<LegacyAbstractPersistenceService> persistenceServices;

  public Mono<MessagingWrapper<Integer>> persist(
      MessagingWrapper<SubmissionMatchDetailEntity> message) {
    return
        Flux
            .fromStream(persistenceServices.stream())
            .filter(service -> service.filterByType(
                    message.parameters().get(ApplicationConstant.SUBMISSION_TYPE).toString()
                )
            )
            .next()
            .flatMap(service ->
                service
                    .loadSubmission(
                        new MessagingWrapper<>(
                            (Integer) message.parameters().get(ApplicationConstant.SUBMISSION_ID),
                            message.parameters()
                        )
                    )
                    .flatMap(service::checkClientData)
                    .flatMap(service::generateForestClient)
                    .flatMap(service::createForestClient)
                    .flatMap(submission ->
                        service
                            .createLocations(submission)
                            .flatMap(service::createContact)
                            .collectList()
                    )
            )
            .map(submissionList ->
                new MessagingWrapper<>(
                    (Integer) submissionList.get(0).parameters()
                        .get(ApplicationConstant.SUBMISSION_ID),
                    submissionList.get(0).parameters()
                )
            );

  }

}
