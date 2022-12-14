package ca.bc.gov.app.m.oracle.legacyclient.service;

import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;
import jakarta.inject.Inject;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LegacyClientService {

  public static final Logger logger = LoggerFactory.getLogger(LegacyClientService.class);

  public static final String DATE_FORMAT = "yyyy-MM-dd";

  @Inject
  private ForestClientRepository forestClientRepository;

  @Inject
  private CoreUtil coreUtil;


  public List<ForestClientEntity> findClientByIncorporationOrName(String incorporationNumber,
                                                                  String companyName) {
    return forestClientRepository.findClientByIncorporationOrName(incorporationNumber, companyName);
  }


  public List<ForestClientEntity> findClientByNameAndBirthdate(String firstName, String lastName,
                                                               String birthdate) {
    Date dateOfBirth = coreUtil.toDate(birthdate, DATE_FORMAT);
    return forestClientRepository.findClientByNameAndBirthdate(firstName, lastName, dateOfBirth);
  }

}
