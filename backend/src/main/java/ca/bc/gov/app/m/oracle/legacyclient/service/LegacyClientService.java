package ca.bc.gov.app.m.oracle.legacyclient.service;

import static ca.bc.gov.app.ApplicationConstant.DATE_FORMAT;

import ca.bc.gov.app.util.CoreUtil;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.repository.ForestClientRepository;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegacyClientService {

  private final ForestClientRepository forestClientRepository;
  private final CoreUtil coreUtil;


  public List<ForestClientEntity> findClientByIncorporationOrName(
      String incorporationNumber,
      String companyName
  ) {
    return forestClientRepository
        .findClientByIncorporationOrName(incorporationNumber, companyName);
  }


  public List<ForestClientEntity> findClientByNameAndBirthdate(
      String firstName,
      String lastName,
      String birthdate
  ) {
    Date dateOfBirth = coreUtil.toDate(birthdate, DATE_FORMAT);
    return forestClientRepository
        .findClientByNameAndBirthdate(firstName, lastName, dateOfBirth);
  }

}
