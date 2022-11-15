package ca.bc.gov.app.m.postgres.client.service.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import ca.bc.gov.app.m.postgres.client.repository.ClientTypeCodeRepository;
import ca.bc.gov.app.m.postgres.client.service.ClientService;

@Service(ClientService.BEAN_NAME)
public class ClientServiceImpl implements ClientService {
	
	@Inject
	private CoreUtil coreUtil;
	
	@Inject
	private ClientTypeCodeRepository clientTypeCodeRepository;

	@Override
	public List<ClientTypeCodeEntity> findActiveClientTypeCodes() {
		Date currentTime = coreUtil.getCurrentTime();
		return clientTypeCodeRepository.findActiveAt(currentTime);
	}

}
