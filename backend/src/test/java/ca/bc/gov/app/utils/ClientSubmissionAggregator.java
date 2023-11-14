package ca.bc.gov.app.utils;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    String incorporationNumber = accessor.getString(2);
    String businessName = accessor.getString(3);
    String clientType = accessor.getString(4);
    String businessType = accessor.getString(5);
    String legalType = accessor.getString(6);
    String goodStanding = accessor.getString(7);
    String birthdateAsString = accessor.getString(8);
    LocalDate birthdate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(birthdateAsString));

    return new ClientBusinessInformationDto(
                incorporationNumber, 
                businessName, 
                businessType,
                clientType, 
                goodStanding, 
                legalType, 
                birthdate);
  }

  private static ClientLocationDto createLocation(ArgumentsAccessor accessor) {

    boolean locationNull = accessor.getBoolean(8);
    if (locationNull) {
      return null;
    }

    boolean addressNull = accessor.getBoolean(9);
    if (addressNull) {
      new ClientLocationDto(null, null);
    }

    boolean addressEmpty = accessor.getBoolean(10);
    if (addressEmpty) {
      return new ClientLocationDto(List.of(), List.of());
    }

    return new ClientLocationDto(
        List.of(createAddress(accessor)),
        List.of()
    );
  }

  private static ClientAddressDto createAddress(ArgumentsAccessor accessor) {

    String streetAddress = accessor.getString(11);
    String country = accessor.getString(12);
    String province = accessor.getString(13);
    String city = accessor.getString(14);
    String postalCode = accessor.getString(15);

    return new ClientAddressDto(
        streetAddress, new ClientValueTextDto(country, country),
        new ClientValueTextDto(province, ""), city, postalCode, 0, "test");
  }

}
