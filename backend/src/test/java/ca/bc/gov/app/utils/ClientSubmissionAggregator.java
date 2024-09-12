package ca.bc.gov.app.utils;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

public class ClientSubmissionAggregator implements ArgumentsAggregator {

  @Override
  public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
      throws ArgumentsAggregationException {
    return new ClientSubmissionDto(
        createBusinessInformation(accessor),
        createLocation(accessor),
        null
    );
  }

  private static ClientBusinessInformationDto createBusinessInformation(
      ArgumentsAccessor accessor) {
    boolean businessInformationNull = accessor.getBoolean(1);
    if (businessInformationNull) {
      return null;
    }

    String registrationNumber = accessor.getString(2);
    String businessName = accessor.getString(3);
    String clientType = accessor.getString(4);
    String businessType = accessor.getString(5);
    String legalType = accessor.getString(6);
    String goodStanding = accessor.getString(7);
    String birthdateAsString = accessor.getString(8);
    LocalDate birthdate =
        StringUtils.isNotBlank(birthdateAsString)
            ? LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(birthdateAsString))
            : null;
    String district = accessor.getString(9);
    String workSafeBcNumber = accessor.getString(33);
    String doingBusinessAs = accessor.getString(34);
    String clientAcronym = accessor.getString(35);
    String firstName = accessor.getString(36);
    String middleName = accessor.getString(37);
    String lastName = accessor.getString(38);
    String submissionNotes = accessor.getString(39);
    String identificationType = accessor.getString(40);
    String clientIdentification = accessor.getString(41);
    String identificationCountry = accessor.getString(42);
    String identificationProvince = accessor.getString(43);

    return new ClientBusinessInformationDto(
                registrationNumber, 
                businessName, 
                businessType,
                clientType, 
                goodStanding, 
                legalType, 
                birthdate,
                district,
                workSafeBcNumber,
                doingBusinessAs,
                clientAcronym,
                firstName,
                middleName,
                lastName,
                submissionNotes,
                new ClientValueTextDto(identificationType,identificationType),
                clientIdentification,
                identificationCountry,
                identificationProvince);
  }

  private static ClientLocationDto createLocation(ArgumentsAccessor accessor) {

    boolean locationNull = accessor.getBoolean(9);
    if (locationNull) {
      return null;
    }

    boolean addressNull = accessor.getBoolean(10);
    if (addressNull) {
      new ClientLocationDto(null, null);
    }

    boolean addressEmpty = accessor.getBoolean(11);
    if (addressEmpty) {
      return new ClientLocationDto(List.of(), List.of());
    }

    return new ClientLocationDto(
        List.of(createAddress(accessor)),
        List.of()
    );
  }

  private static ClientAddressDto createAddress(ArgumentsAccessor accessor) {

    String streetAddress = accessor.getString(12);
    String country = accessor.getString(13);
    String province = accessor.getString(14);
    String city = accessor.getString(15);
    String postalCode = accessor.getString(16);
    String complementaryAddressOne = accessor.getString(25);
    String complementaryAddressTwo = accessor.getString(26);
    String businessPhoneNumber = accessor.getString(27);
    String secondaryPhoneNumber = accessor.getString(28);
    String faxNumber = accessor.getString(29);
    String emailAddress = accessor.getString(30);
    String contactNotes = accessor.getString(31);

    return new ClientAddressDto(
        streetAddress,
        complementaryAddressOne,
        complementaryAddressTwo,
        new ClientValueTextDto(country, country),
        new ClientValueTextDto(province, ""),
        city,
        postalCode,
        businessPhoneNumber,
        secondaryPhoneNumber,
        faxNumber,
        emailAddress,
        contactNotes,
        0,
        "test");
  }

}
