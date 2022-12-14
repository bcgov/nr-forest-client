package ca.bc.gov.app.m.postgres.client.service;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.core.vo.CodeDescrVO;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import ca.bc.gov.app.m.postgres.client.repository.ClientTypeCodeRepository;
import jakarta.inject.Inject;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

  @Inject
  private CoreUtil coreUtil;

  @Inject
  private ClientTypeCodeRepository clientTypeCodeRepository;


  public List<CodeDescrVO> findActiveClientTypeCodes() {
    Date currentTime = coreUtil.getCurrentTime();
    List<ClientTypeCodeEntity> clientTypeCodes = clientTypeCodeRepository.findActiveAt(currentTime);
    return coreUtil.toSortedCodeDescrVOs(clientTypeCodes);
  }

}
