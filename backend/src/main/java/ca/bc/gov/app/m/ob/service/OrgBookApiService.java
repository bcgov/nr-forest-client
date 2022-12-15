package ca.bc.gov.app.m.ob.service;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.m.ob.dto.OrgBookResponseDTO;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;
import ca.bc.gov.app.m.oracle.legacyclient.dto.ClientPublicViewDTO;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrgBookApiService {

  private final CoreUtil coreUtil;
  private final ClientDoingBusinessAsRepository clientDoingBusinessAsRepository;
  private final ForestClientRepository forestClientRepository;


  public OrgBookResponseDTO findByClientName(String clientName) {
    String uri;
    try {
      uri = "https://orgbook.gov.bc.ca/api/v3/search/autocomplete?q=" +
          URLEncoder.encode(clientName, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to encode url parameter " + e.toString());
    }

    RestTemplate restTemplate = new RestTemplate();
    String restCallResponse = restTemplate.getForObject(toURI(uri), String.class);

    OrgBookResponseDTO response =
        coreUtil.jsonStringToObj(restCallResponse, OrgBookResponseDTO.class);

    log.info("Results: " + response.results());

    return response;
  }


  public ResponseEntity<Object> findByIncorporationNumber(String incorporationNumber) {
    String url =
        "https://orgbook.gov.bc.ca/api/v4/search/topic?format=json&inactive=any&latest=true&ordering=-score&q=" +
            incorporationNumber + "&revoked=false";
    RestTemplate restTemplate = new RestTemplate();
    String restCallResponse = restTemplate.getForObject(toURI(url), String.class);

    OrgBookResponseDTO response =
        coreUtil.jsonStringToObj(restCallResponse, OrgBookResponseDTO.class);

    log.info("Response: " + response.toString());
    log.info("Results: " + response.results());

    return ResponseEntity.ok(response);
  }

  private URI toURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Failed to convert input value to URI. " + e.toString());
    }
  }


  public List<ClientDoingBusinessAsEntity> validateClientDoingBusinessAs() {

    Date startedDate = new Date();

    Long count = clientDoingBusinessAsRepository.countAll();
    List<ClientDoingBusinessAsEntity> doingBusinessClients = new ArrayList<>();
    List<ClientPublicViewDTO> clients = new ArrayList<>();

    int take = 10;
    int numberOfPages = (int) Math.ceil(count / take);

    for (int page = 1; page <= numberOfPages; page++) {
      doingBusinessClients.addAll(
          clientDoingBusinessAsRepository.findAllPagable(PageRequest.of(page, take)));
    }

    for (ClientDoingBusinessAsEntity clientDoingBusinessAsEntity : doingBusinessClients) {
      OrgBookResponseDTO orgBookResponseDTO = this.findByClientName(
          clientDoingBusinessAsEntity.getDoingBusinessAsName());

      clients.add(
          new ClientPublicViewDTO(
              clientDoingBusinessAsEntity.getClientNumber(),
              null,
              clientDoingBusinessAsEntity.getDoingBusinessAsName(),
              null,
              null,
              null,
              null,
              !orgBookResponseDTO.results().isEmpty() ?
                  orgBookResponseDTO.results().get(0).value() :
                  null
          )
      );
    }

    //log.info(coreUtil.objToJsonString(clients));
    String content = coreUtil.objToJsonString(clients);
    String path = "C:/repo/a.txt";
    try {
      Files.write(Paths.get(path), content.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }

    log.info("Started at " + startedDate);
    log.info("Finished at " + new Date());
    return doingBusinessClients;
  }


  public List<ForestClientEntity> validateUnregisteredCompanies() {
    Date startedDate = new Date();

    Long count = forestClientRepository.countAll();
    List<ForestClientEntity> unregisteredCompanies = new ArrayList<>();
    List<ClientPublicViewDTO> clients = new ArrayList<>();

    int take = 10;
    int numberOfPages = (int) Math.ceil(count / take);

    for (int page = 1; page <= numberOfPages; page++) {
      unregisteredCompanies.addAll(
          forestClientRepository.findAllPagable(PageRequest.of(page, take)));
    }

    for (ForestClientEntity forestClientEntity : unregisteredCompanies) {
      OrgBookResponseDTO orgBookResponseDTO =
          this.findByClientName(forestClientEntity.getClientName());

      String nameInOrgBook = !orgBookResponseDTO.results().isEmpty() ?
          orgBookResponseDTO.results().get(0).value() :
          null;

      clients.add(new ClientPublicViewDTO(
          forestClientEntity.getClientNumber(),
          coreUtil
              .isSame(forestClientEntity.getClientName(), nameInOrgBook) ?
              orgBookResponseDTO.results().get(0).topic_source_id() :
              null
          ,
          forestClientEntity.getClientName(),
          null,
          null,
          null,
          null,
          nameInOrgBook
      ));
    }

    String content = coreUtil.objToJsonString(clients);
    String path = "C:/repo/a.txt";
    try {
      Files.write(Paths.get(path), content.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }

    log.info("Started at " + startedDate);
    log.info("Finished at " + new Date());
    return unregisteredCompanies;
  }

}
