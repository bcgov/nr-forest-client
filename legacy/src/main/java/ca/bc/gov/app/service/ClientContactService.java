package ca.bc.gov.app.service;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class ClientContactService {

  private final R2dbcEntityOperations entityTemplate;
  private final ForestClientContactRepository repository;
  private final AbstractForestClientMapper<ForestClientContactDto, ForestClientContactEntity> mapper;

  public Mono<String> saveAndGetIndex(ForestClientContactDto dto) {
    return
        Mono
            .just(dto)
            .filterWhen(locationDto ->
                locateClientContact(
                    locationDto.clientNumber(),
                    locationDto.clientLocnCode(),
                    locationDto.contactName()
                )
                    .map(forestClientLocation -> false) // means you can't create it
                    .defaultIfEmpty(true) // means you can create it
            )
            .map(mapper::toEntity)
            .flatMap(entity -> getNextContactId().map(entity::withClientContactId))
            .flatMap(entity -> entityTemplate
                .insert(ForestClientContactEntity.class)
                .using(entity)
            )
            .doOnNext(forestClientContact ->
                log.info(
                    "Saved forest client contact {} {}",
                    forestClientContact.getClientNumber(),
                    forestClientContact.getContactName()
                )
            )
            .map(ForestClientContactEntity::getClientNumber);
  }

  public Flux<ForestClientContactDto> search(
      String firstName,
      String lastName,
      String email,
      String phone
  ) {
    return
        repository
            .matchBy(String.join(" ", firstName, lastName), email, phone)
            .map(mapper::toDto);
  }

  private Flux<ForestClientContactEntity> locateClientContact(
      String clientNumber,
      String locationCode,
      String contactName
  ) {
    return
        entityTemplate
            .select(
                Query
                    .query(
                        Criteria
                            .where(ApplicationConstants.CLIENT_NUMBER)
                            .is(clientNumber)
                            .and("CLIENT_LOCN_CODE").is(locationCode)
                            .and("CONTACT_NAME").is(contactName.toUpperCase())
                    ),
                ForestClientContactEntity.class
            )
            .doOnNext(forestClientLocation -> log.info(
                    "Forest client location {} {} already exists",
                    forestClientLocation.getClientLocnCode(),
                    forestClientLocation.getContactName()
                )
            );
  }

  private Mono<String> getNextContactId() {
    return entityTemplate
        .getDatabaseClient()
        .sql("SELECT THE.client_contact_seq.NEXTVAL FROM dual")
        .fetch()
        .first()
        .map(row -> row.get("NEXTVAL"))
        .map(String::valueOf);
  }

}
