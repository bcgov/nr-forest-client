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
        createLocation(accessor)
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

    return new ClientBusinessInformationDto(
                registrationNumber, 
                businessName, 
                businessType,
                clientType, 
                goodStanding, 
                legalType, 
                birthdate,
                district);
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

    return new ClientAddressDto(
        streetAddress, new ClientValueTextDto(country, country),
        new ClientValueTextDto(province, ""), city, postalCode, 0, "test");
  }

}
