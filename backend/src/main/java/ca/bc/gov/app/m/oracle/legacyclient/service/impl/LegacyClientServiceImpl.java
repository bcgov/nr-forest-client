package ca.bc.gov.app.m.oracle.legacyclient.service.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;
import ca.bc.gov.app.m.oracle.legacyclient.service.LegacyClientService;

@Service(LegacyClientService.BEAN_NAME)
public class LegacyClientServiceImpl implements LegacyClientService {

	public static final Logger logger = LoggerFactory.getLogger(LegacyClientServiceImpl.class);

	@Inject
	private ForestClientRepository forestClientRepository;

	@Override
	public List<ForestClientEntity> findClientByIncorporationOrName(String incorporationNumber, String companyName) {
		return forestClientRepository.findClientByIncorporationOrName(incorporationNumber, companyName);
	}

	@Override
	public List<ForestClientEntity> findClientIndividualByNameAndDOB(String firstName, String lastName,
			Date birthdate) {
		return forestClientRepository.findClientIndividualByNameAndDOB(firstName, lastName, birthdate);

	}

}
