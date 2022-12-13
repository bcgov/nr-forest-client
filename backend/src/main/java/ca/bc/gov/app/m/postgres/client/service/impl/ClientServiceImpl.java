package ca.bc.gov.app.m.postgres.client.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.core.vo.CodeDescrVO;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import ca.bc.gov.app.m.postgres.client.repository.ClientTypeCodeRepository;
import ca.bc.gov.app.m.postgres.client.service.ClientService;
import jakarta.inject.Inject;

@Service(ClientService.BEAN_NAME)
public class ClientServiceImpl implements ClientService {
	
	@Inject
	private CoreUtil coreUtil;
	
	@Inject
	private ClientTypeCodeRepository clientTypeCodeRepository;

	@Override
	public List<CodeDescrVO> findActiveClientTypeCodes() {
		Date currentTime = coreUtil.getCurrentTime();
		List<ClientTypeCodeEntity> clientTypeCodes = clientTypeCodeRepository.findActiveAt(currentTime);
		return coreUtil.toSortedCodeDescrVOs(clientTypeCodes);	
	}

}
