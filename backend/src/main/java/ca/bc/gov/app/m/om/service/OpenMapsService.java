package ca.bc.gov.app.m.om.service;

import ca.bc.gov.app.util.CoreUtil;
import ca.bc.gov.app.m.om.dto.FirstNationBandValidationDTO;
import ca.bc.gov.app.m.om.dto.OpenMapsResponseDTO;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientLocationEntity;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ClientLocationRepository;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenMapsService {

  private final ForestClientRepository forestClientRepository;
  private final ClientLocationRepository clientLocationRepository;
  private final CoreUtil coreUtil;

  private URI toURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Failed to convert input value to URI. " + e.toString());
    }
  }

  private OpenMapsResponseDTO checkSourceFirstNationId(String firstNationId) {

    try {
      String url =
          "https://openmaps.gov.bc.ca/geo/pub/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP&count=10000&"
              + "CQL_FILTER=FIRST_NATION_FEDERAL_ID=" + firstNationId + "&outputFormat=json";

      RestTemplate restTemplate = new RestTemplate();
      String restCallResponse = restTemplate.getForObject(toURI(url), String.class);

      OpenMapsResponseDTO response =
          coreUtil.jsonStringToObj(restCallResponse, OpenMapsResponseDTO.class);

      return response;

    } catch (RuntimeException e) {
      log.error("Failed to get source data for " + firstNationId + e.toString());
      return null;
    }

  }

  public List<FirstNationBandValidationDTO> validateFirstNationBand() {

    List<ForestClientEntity> clients = forestClientRepository.findAllFirstNationBandClients();

    List<FirstNationBandValidationDTO> firstNationBands = new ArrayList<>();
    int notFound = 0;
    int match = 0;
    int partialMatch = 0;
    int notMatch = 0;

    int nameMatch = 0;
    int addressMatch = 0;

    for (ForestClientEntity client : clients) {
      if (client.getCorpRegnNmbr() != null) {
        OpenMapsResponseDTO response = checkSourceFirstNationId(client.getCorpRegnNmbr());
        ClientLocationEntity clientLocation = clientLocationRepository
            .findByClientNumber(client.getClientNumber());
        if (!CollectionUtils.isEmpty(response.features())) {
          FirstNationBandValidationDTO firstNationBand =
              new FirstNationBandValidationDTO(
                  client.getClientNumber(),
                  client.getCorpRegnNmbr(),
                  client.getClientName(),
                  response.features().get(0).properties().firstNationFederalName(),
                  null,
                  clientLocation.getAddressOne(),
                  response.features().get(0).properties().addressLine1(),
                  clientLocation.getAddressTwo(),
                  response.features().get(0).properties().addressLine2(),
                  clientLocation.getCity(), response.features().get(0).properties().officeCity(),
                  clientLocation.getProvince(),
                  response.features().get(0).properties().officeProvince(),
                  clientLocation.getPostalCode(),
                  response.features().get(0).properties().officePostalCode(),
                  null
              );

          firstNationBands.add(firstNationBand);
          if (firstNationBand.nameMatch() && firstNationBand.addressMatch()) {
            match += 1;
            nameMatch += 1;
            addressMatch += 1;
          } else if (firstNationBand.nameMatch()) {
            nameMatch += 1;
            partialMatch += 1;
          } else if (firstNationBand.addressMatch()) {
            addressMatch += 1;
            partialMatch += 1;
          } else {
            notMatch += 1;
          }
        } else {
          FirstNationBandValidationDTO firstNationBand =
              new FirstNationBandValidationDTO(
                  client.getClientNumber(),
                  client.getCorpRegnNmbr(),
                  client.getClientName(),
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null
              );
          notFound += 1;
          firstNationBands.add(firstNationBand);
        }
      } else {
        FirstNationBandValidationDTO firstNationBand = new FirstNationBandValidationDTO(
            client.getClientNumber(),
            client.getCorpRegnNmbr(),
            client.getClientName(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        notFound += 1;
        firstNationBands.add(firstNationBand);
      }
    }

    log.info("not found: " + notFound);
    log.info("match: " + match);
    log.info("nameMatch: " + nameMatch);
    log.info("addressMatch: " + addressMatch);
    log.info("partialMatch: " + partialMatch);
    log.info("notMatch: " + notMatch);

    return firstNationBands;

  }

}
