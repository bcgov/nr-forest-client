package ca.bc.gov.app.m.postgres.client.service;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.core.vo.CodeDescrVO;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import ca.bc.gov.app.m.postgres.client.repository.ClientTypeCodeRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

  @Autowired
  private CoreUtil coreUtil;

  @Autowired
  private ClientTypeCodeRepository clientTypeCodeRepository;


  public List<CodeDescrVO> findActiveClientTypeCodes() {
    Date currentTime = coreUtil.getCurrentTime();
    List<ClientTypeCodeEntity> clientTypeCodes = clientTypeCodeRepository.findActiveAt(currentTime);
    return coreUtil.toSortedCodeDescrVOs(clientTypeCodes);
  }

}
