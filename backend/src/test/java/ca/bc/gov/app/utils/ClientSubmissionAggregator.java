package ca.bc.gov.app.utils;

import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import java.util.ArrayList;
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
        accessor.getString(0),
        createBusinessInformation(accessor),
        createLocation(accessor),
        createSubmitterInformation(accessor));
  }

  private static ClientBusinessInformationDto createBusinessInformation(
      ArgumentsAccessor accessor) {
    boolean businessInformationNull = accessor.getBoolean(1);
    if (businessInformationNull) {
      return null;
    }

    String firstName = accessor.getString(2);
    String lastName = accessor.getString(3);
    String birthdate = accessor.getString(4);
    String incorporationNumber = accessor.getString(5);
    String doingBusinessAsName = accessor.getString(6);
    String businessName = accessor.getString(7);
    ClientTypeEnum clientType = ClientTypeEnum.valueOf(accessor.getString(8));
    BusinessTypeEnum businessType = BusinessTypeEnum.valueOf(accessor.getString(9));
    LegalTypeEnum legalType = LegalTypeEnum.valueOf(accessor.getString(10));
    String goodStanding = accessor.getString(11);

    return new ClientBusinessInformationDto(firstName, lastName, birthdate, incorporationNumber,
        doingBusinessAsName, businessName, clientType, businessType, legalType, goodStanding);
  }

  private static ClientLocationDto createLocation(ArgumentsAccessor accessor) {

    boolean locationNull = accessor.getBoolean(12);
    if (locationNull) {
      return null;
    }

    boolean addressNull = accessor.getBoolean(13);
    if (addressNull) {
      new ClientLocationDto(null);
    }

    boolean addressEmpty = accessor.getBoolean(14);
    if (addressEmpty) {
      return new ClientLocationDto(new ArrayList<>());
    }

    return new ClientLocationDto(
        List.of(createAddress(accessor))
    );
  }

  private static ClientAddressDto createAddress(ArgumentsAccessor accessor) {

    String streetAddress = accessor.getString(15);
    String country = accessor.getString(16);
    String province = accessor.getString(17);
    String city = accessor.getString(18);
    String postalCode = accessor.getString(19);
    List<ClientContactDto> contacts;

    boolean contactsNull = accessor.getBoolean(20);
    boolean contactsEmpty = accessor.getBoolean(21);
    if (contactsNull) {
      contacts = null;
    } else if (contactsEmpty) {
      contacts = new ArrayList<>();
    } else {
      contacts = List.of(createContact(accessor));
    }

    return new ClientAddressDto(
        streetAddress, new ClientValueTextDto(country, ""),
        new ClientValueTextDto(province, ""), city, postalCode, 0, contacts);
  }

  private static ClientContactDto createContact(ArgumentsAccessor accessor) {
    String contactType = accessor.getString(22);
    String contactFirstName = accessor.getString(23);
    String contactLastName = accessor.getString(24);
    String contactPhoneNumber = accessor.getString(25);
    String contactEmail = accessor.getString(26);

    return new ClientContactDto(
        new ClientValueTextDto(contactType, contactType), contactFirstName, contactLastName,
        contactPhoneNumber, contactEmail, 0);
  }

  private static ClientSubmitterInformationDto createSubmitterInformation(
      ArgumentsAccessor accessor) {

    boolean submitterInformationNull = accessor.getBoolean(27);
    if (submitterInformationNull) {
      return null;
    }

    String submitterFirstName = accessor.getString(28);
    String submitterLastName = accessor.getString(29);
    String submitterPhoneNumber = accessor.getString(30);
    String submitterEmail = accessor.getString(31);

    return new ClientSubmitterInformationDto(
        submitterFirstName, submitterLastName, submitterPhoneNumber, submitterEmail);
  }
}
