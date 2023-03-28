package ca.bc.gov.app.utils;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientBusinessTypeDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
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
        createBusinessType(accessor),
        createBusinessInformation(accessor),
        createLocation(accessor),
        createSubmitterInformation(accessor));
  }

  private static ClientBusinessTypeDto createBusinessType(ArgumentsAccessor accessor) {
    boolean businessTypeNull = accessor.getBoolean(0);

    if (businessTypeNull) {
      return null;
    }

    boolean clientTypeNull = accessor.getBoolean(1);
    if (clientTypeNull) {
      return new ClientBusinessTypeDto(null);
    }

    String clientType = accessor.getString(2);
    return new ClientBusinessTypeDto(new ClientValueTextDto(clientType, ""));
  }

  private static ClientBusinessInformationDto createBusinessInformation(
      ArgumentsAccessor accessor) {
    boolean businessInformationNull = accessor.getBoolean(3);
    if (businessInformationNull) {
      return null;
    }

    String firstName = accessor.getString(4);
    String lastName = accessor.getString(5);
    String birthdate = accessor.getString(6);
    String incorporationNumber = accessor.getString(7);
    String doingBusinessAsName = accessor.getString(8);
    String businessName = accessor.getString(9);

    return new ClientBusinessInformationDto(firstName, lastName, birthdate,
        incorporationNumber, doingBusinessAsName, businessName);
  }

  private static ClientLocationDto createLocation(ArgumentsAccessor accessor) {

    boolean locationNull = accessor.getBoolean(10);
    if (locationNull) {
      return null;
    }

    boolean addressNull = accessor.getBoolean(11);
    if (addressNull) {
      new ClientLocationDto(null);
    }

    boolean addressEmpty = accessor.getBoolean(12);
    if (addressEmpty) {
      return new ClientLocationDto(new ArrayList<>());
    }

    return new ClientLocationDto(
        List.of(createAddress(accessor))
    );
  }

  private static ClientAddressDto createAddress(ArgumentsAccessor accessor) {

    String streetAddress = accessor.getString(13);
    String country = accessor.getString(14);
    String province = accessor.getString(15);
    String city = accessor.getString(16);
    String postalCode = accessor.getString(17);
    List<ClientContactDto> contacts;

    boolean contactsNull = accessor.getBoolean(18);
    boolean contactsEmpty = accessor.getBoolean(19);
    if(contactsNull) {
      contacts = null;
    } else if(contactsEmpty) {
      contacts = new ArrayList<>();
    } else {
      contacts = List.of(createContact(accessor));
    }

    return new ClientAddressDto(
        streetAddress, new ClientValueTextDto(country, ""),
        new ClientValueTextDto(province, ""), city, postalCode, contacts);
  }

  private static ClientContactDto createContact(ArgumentsAccessor accessor) {
    String contactType = accessor.getString(20);
    String contactFirstName = accessor.getString(21);
    String contactLastName = accessor.getString(22);
    String contactPhoneNumber = accessor.getString(23);
    String contactEmail = accessor.getString(24);

    return new ClientContactDto(
        contactType, contactFirstName, contactLastName, contactPhoneNumber, contactEmail, 0);
  }

  private static ClientSubmitterInformationDto createSubmitterInformation(
      ArgumentsAccessor accessor) {

    boolean submitterInformationNull = accessor.getBoolean(25);
    if(submitterInformationNull) {
      return null;
    }

    String submitterFirstName = accessor.getString(26);
    String submitterLastName = accessor.getString(27);
    String submitterPhoneNumber = accessor.getString(28);
    String submitterEmail = accessor.getString(29);

    return new ClientSubmitterInformationDto(
        submitterFirstName, submitterLastName, submitterPhoneNumber, submitterEmail);
  }
}
