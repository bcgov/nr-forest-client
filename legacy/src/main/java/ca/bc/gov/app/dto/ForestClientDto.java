package ca.bc.gov.app.dto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.annotation.Transient;

public record ForestClientDto(
    String clientNumber,
    String clientName,
    String legalFirstName,
    String legalMiddleName,
    String clientStatusCode,
    String clientTypeCode,
    LocalDate birthdate,
    String clientIdTypeCode,
    String clientIdentification,
    String registryCompanyTypeCode,
    String corpRegnNmbr,
    String clientComment,
    String createdBy,
    String updatedBy,
    Long orgUnit
) {

  @Transient
  public String name(){
    if(Objects.equals(this.clientTypeCode, "I")){
      return Stream.of(this.legalFirstName, this.legalMiddleName, this.clientName)
          .filter(Objects::nonNull)
          .collect(Collectors.joining(" "));
    }else{
      return this.clientName;
    }
  }

}
