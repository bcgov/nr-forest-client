package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.validateEmail;
import static ca.bc.gov.app.util.ClientValidationUtils.validatePhoneNumber;
import java.util.Arrays;
import java.util.regex.Pattern;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.entity.client.ContactTypeCodeEntity;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientContactDtoValidator implements Validator {

  private final ContactTypeCodeRepository typeCodeRepository;
  
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s'-]+$");
  private static final String FIRST_NAME_FIELD = "firstName";
  private static final String LAST_NAME_FIELD = "lastName";

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientContactDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientContactDto contact = (ClientContactDto) target;
    
    validateContactNames(errors, contact);

    validateContactType(contact, errors);

    validatePhoneNumber(
        contact.phoneNumber(), "phoneNumber", errors);

    validateEmail(contact.email(), "email", errors);
  }
  
  private void validateContactNames(Errors errors, ClientContactDto contact) {
    Arrays.asList(FIRST_NAME_FIELD, LAST_NAME_FIELD).forEach(field -> {
      String fieldValue = switch (field) {
        case FIRST_NAME_FIELD -> contact.firstName();
        case LAST_NAME_FIELD -> contact.lastName();
        default -> "";
      };
      String fieldName = field.equals(FIRST_NAME_FIELD) ? "first name" : "last name";

      if (StringUtils.isBlank(fieldValue)) {
        errors.rejectValue(field, String.format("All contacts must have a %s.", fieldName));
      }
      else if (!NAME_PATTERN.matcher(fieldValue).matches()) {
        errors.rejectValue(field, String.format("%s has an invalid character.", fieldValue));
      }
      else if (StringUtils.length(fieldValue) > 30) {
        errors.rejectValue(field, "This field has a 30 character limit.");
      }
    });
  }

  @SneakyThrows
  private void validateContactType(ClientContactDto contact, Errors errors) {
	String contactTypeField = "contactType";
	
    if (contact.contactType() == null || StringUtils.isBlank(contact.contactType().value())) {
      errors.rejectValue(contactTypeField, "All contacts must select a role.");
      return;
    }

    ContactTypeCodeEntity contactTypeCode = typeCodeRepository
        .findById(contact.contactType().value()).toFuture().get();

    if (null == contactTypeCode) {
      errors.rejectValue(
               contactTypeField, "Contact Type " 
               + contact.contactType().text()
               + " is invalid");
    }
  }

}
