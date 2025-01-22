package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import ca.bc.gov.app.service.ClientContactService;
import ca.bc.gov.app.utils.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(2)
public class ClientPatchOperationContactService implements ClientPatchOperationService {

  private final ForestClientContactRepository contactRepository;
  private final ClientContactService clientContactService;
  private final AbstractForestClientMapper<ForestClientContactDto, ForestClientContactEntity> contactMapper;

  @Override
  public String getPrefix() {
    return "contacts";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of(
        "/contactCode",
        "/contactName",
        "/businessPhone",
        "/secondaryPhone",
        "/faxNumber",
        "/emailAddress"
    );
  }

  /**
   * Applies a JSON patch to the client's contacts. This method handles the removal, addition, and
   * updating of contacts based on the operations specified in the JSON patch. It starts with the
   *
   * @param clientNumber the client number to which the contacts belong
   * @param contactPatch the JSON patch containing the operations to be applied
   * @param mapper       the ObjectMapper to parse JSON nodes
   * @return a Mono that completes when all patch operations have been applied
   */
  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonPatch contactPatch, ObjectMapper mapper) {

    Mono<Void> removeMono =
        Flux
            .fromIterable(removePatchedContacts(contactPatch, mapper))
            .map(Long::valueOf)
            .filterWhen(contactRepository::existsById)

            // After checking of that ID exists, we will need to load all entries with the same
            // client number and contact name, due to the multiple contact-location association.
            .flatMap(contactRepository::findById)
            .flatMap(contact ->
                contactRepository
                    .findAllByClientNumberAndContactName(
                        contact.getClientNumber(),
                        contact.getContactName()
                    )
                    .map(ForestClientContactEntity::getClientContactId)
            )

            .doOnNext(contactId -> log.info("Removing Forest Client Contact with id {}", contactId))
            .flatMap(contactRepository::deleteById)
            .collectList()
            .then();

    Mono<Void> addMono =
        Flux
            .fromIterable(addPatchedContact(contactPatch, clientNumber, mapper))
            .doOnNext(contact -> log.info("Adding Forest Client Contact {}", contact))
            .flatMap(clientContactService::saveAndGetIndex)
            .collectList()
            .then();

    Mono<Void> updateMono =
        updatePatchedContact(contactPatch, mapper)
            .doOnNext(contact -> log.info("Updating Forest Client Contact {}", contact))
            .flatMap(contactRepository::save)
            .collectList()
            .then();

    return removeMono.then(addMono).then(updateMono);
  }

  /**
   * Adds new contacts specified in the JSON patch. It filters the JSON patch to only include add
   * operations on the contacts array and creates new contact entities.
   *
   * @param patch        the JSON patch containing the add operations
   * @param clientNumber the client number to associate with the new contacts
   * @param mapper       the ObjectMapper to parse JSON nodes
   * @return a list of newly added ForestClientContactEntity objects
   */
  private List<ForestClientContactDto> addPatchedContact(
      JsonPatch patch,
      String clientNumber,
      ObjectMapper mapper
  ) {

    List<ForestClientContactDto> addedContact = new ArrayList<>();

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(patch, "add", getPrefix(), mapper);

    filteredNode.forEach(node ->
        addedContact.add(
            PatchUtils
                .loadAddValue(
                    node,
                    ForestClientContactDto.class,
                    mapper
                )
                .withClientNumber(clientNumber)
        )
    );

    return addedContact;
  }

  /**
   * Updates the contacts specified in the JSON patch. It filters the JSON patch to only include
   * replace operations on the contacts array and applies the changes to the existing contacts. It
   * only updates the fields specified in the restricted paths.
   *
   * @param patch  the JSON patch containing the replace operations
   * @param mapper the ObjectMapper to parse JSON nodes
   * @return a Flux of updated ForestClientContactEntity objects
   */
  private Flux<ForestClientContactEntity> updatePatchedContact(JsonPatch patch,
      ObjectMapper mapper) {

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    Set<Long> contactIds = PatchUtils
        .loadIds(filteredNode)
        .stream()
        .map(Long::valueOf)
        .collect(Collectors.toSet());

    return
        // Due to the oracle nature of having multiple entries for the same person due to the
        // multiple contact-location association, we will have to use the data to find the
        // other contacts as well
        contactRepository
            .findAllById(contactIds)
            // We have to go for the client number and contact name as the contact id is unique
            // but that's just a single contact
            .flatMap(contact -> patchContact(mapper, contact, filteredNode));
  }

  private Flux<ForestClientContactEntity> patchContact(
      ObjectMapper mapper,
      ForestClientContactEntity contact,
      JsonNode filteredNode
  ) {
    return contactRepository.
        findAllByClientNumberAndContactName(
            contact.getClientNumber(),
            contact.getContactName()
        )
        // Then we just update it with the patch
        .flatMap(currentContact ->
            Mono
                .just(PatchUtils.patchClient(
                        PatchUtils.filterPatchOperations(
                            filteredNode,
                            contact.getClientContactId().toString(),
                            getRestrictedPaths(),
                            mapper
                        ),
                        currentContact,
                        ForestClientContactEntity.class,
                        mapper
                    )
                )
                .filter(client -> !currentContact.equals(client))
                .doOnNext(
                    client -> log.info("Detected Forest Client Contact changes {}", client))
        );
  }

  /**
   * List contacts specified in the JSON patch to be removed. It filters the JSON patch to only
   * include remove operations on the contacts array.
   *
   * @param patch  the JSON patch containing the remove operations
   * @param mapper the ObjectMapper to parse JSON nodes
   * @return a list of contact IDs to be removed
   */
  private List<String> removePatchedContacts(JsonPatch patch, ObjectMapper mapper) {

    List<String> removedContacts = new ArrayList<>();

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(patch, "remove", getPrefix(), mapper);

    filteredNode.forEach(node ->
        removedContacts.add(
            node.get("path").asText().replace("/", StringUtils.EMPTY))
    );

    return removedContacts;
  }

}
