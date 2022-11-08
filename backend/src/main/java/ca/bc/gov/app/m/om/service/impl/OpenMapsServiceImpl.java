package ca.bc.gov.app.m.om.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.m.om.service.OpenMapsService;
import ca.bc.gov.app.m.om.vo.FirstNationBandVidationVO;
import ca.bc.gov.app.m.om.vo.OpenMapsResponseVO;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientLocationEntity;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ClientLocationRepository;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;

@Service(OpenMapsService.BEAN_NAME)
public class OpenMapsServiceImpl implements OpenMapsService {

	@Inject
	private ForestClientRepository forestClientRepository;

	@Inject
	private ClientLocationRepository clientLocationRepository;

	@Inject
	private CoreUtil coreUtil;

	public static final Logger logger = LoggerFactory.getLogger(OpenMapsServiceImpl.class);

	private URI toURI(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Failed to convert input value to URI. " + e.toString());
		}
	}

	private OpenMapsResponseVO checkSourceFirstNationId(String firstNationId) {

		try {
			String url = "https://openmaps.gov.bc.ca/geo/pub/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP&count=10000&"
					+ "CQL_FILTER=FIRST_NATION_FEDERAL_ID=" + firstNationId + "&outputFormat=json";
			
			RestTemplate restTemplate = new RestTemplate();
			String restCallResponse = restTemplate.getForObject(toURI(url), String.class);

			OpenMapsResponseVO response = coreUtil.jsonStringToObj(restCallResponse, OpenMapsResponseVO.class);

			return response;

		} catch (RuntimeException e) {
			logger.error("Failed to get source data for " + firstNationId + e.toString());
			return null;
		}

	}

	@Override
	public List<FirstNationBandVidationVO> validateFirstNationBand() {

		List<ForestClientEntity> clients = forestClientRepository.findAllFirstNationBandClients();

		List<FirstNationBandVidationVO> firstNationBands = new ArrayList<>();
		int notFound = 0;
		int match = 0;
		int partialMatch = 0;
		int notMatch = 0;

		int nameMatch = 0;
		int addressMatch = 0;

		for (ForestClientEntity client : clients) {
			if (client.getCorpRegnNmbr() != null) {
				OpenMapsResponseVO response = checkSourceFirstNationId(client.getCorpRegnNmbr());
				ClientLocationEntity clientLocation = clientLocationRepository
						.findByClientNumber(client.getClientNumber());
				if (!CollectionUtils.isEmpty(response.features)) {
					FirstNationBandVidationVO firstNationBand = new FirstNationBandVidationVO(client.getClientNumber(),
							client.getCorpRegnNmbr(), client.getClientName(),
							response.features.get(0).properties.FIRST_NATION_FEDERAL_NAME,
							clientLocation.getAddressOne(), response.features.get(0).properties.ADDRESS_LINE1,
							clientLocation.getAddressTwo(), response.features.get(0).properties.ADDRESS_LINE2,
							clientLocation.getCity(), response.features.get(0).properties.OFFICE_CITY,
							clientLocation.getProvince(), response.features.get(0).properties.OFFICE_PROVINCE,
							clientLocation.getPostalCode(), response.features.get(0).properties.OFFICE_POSTAL_CODE);

					firstNationBands.add(firstNationBand);
					if (firstNationBand.nameMatch && firstNationBand.addressMatch) {
						match += 1;
						nameMatch += 1;
						addressMatch += 1;
					} else if (firstNationBand.nameMatch) {
						nameMatch += 1;
						partialMatch += 1;
					} else if (firstNationBand.addressMatch) {
						addressMatch += 1;
						partialMatch += 1;
					} else {
						notMatch += 1;
					}
				} else {
					FirstNationBandVidationVO firstNationBand = new FirstNationBandVidationVO();
					firstNationBand.clientNumber = client.getClientNumber();
					firstNationBand.corpRegnNmbr = client.getCorpRegnNmbr();
					firstNationBand.clientName = client.getClientName();
					notFound += 1;
					firstNationBands.add(firstNationBand);
				}
			} else {
				FirstNationBandVidationVO firstNationBand = new FirstNationBandVidationVO();
				firstNationBand.clientNumber = client.getClientNumber();
				firstNationBand.corpRegnNmbr = client.getCorpRegnNmbr();
				firstNationBand.clientName = client.getClientName();
				notFound += 1;
				firstNationBands.add(firstNationBand);
			}
		}
		
		logger.info("not found: " + notFound);
		logger.info("match: " + match);
		logger.info("nameMatch: " + nameMatch);
		logger.info("addressMatch: " + addressMatch);
		logger.info("partialMatch: " + partialMatch);
		logger.info("notMatch: " + notMatch);
		
		return firstNationBands;

	}

}
