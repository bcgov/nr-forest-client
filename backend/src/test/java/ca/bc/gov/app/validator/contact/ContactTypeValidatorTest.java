package ca.bc.gov.app.validator.contact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.entity.client.ContactTypeCodeEntity;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | ContactTypeValidator")
class ContactTypeValidatorTest {


  private final ContactTypeCodeRepository repository = mock(ContactTypeCodeRepository.class);
  private final ContactTypeValidator validator = new ContactTypeValidator(repository);

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(
      String contactCode,
      boolean hasInDb,
      String expectedMessage
  ) {

    if (hasInDb) {
      when(repository.findById(contactCode))
          .thenReturn(Mono.justOrEmpty(ContactTypeCodeEntity.builder().build()));
    } else {
      when(repository.findById(contactCode))
          .thenReturn(Mono.empty());
    }

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientContactDto(
                    new ClientValueTextDto(contactCode, StringUtils.EMPTY),
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    0,
                    List.of()
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.contacts[0].contactType",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of("BL", true, StringUtils.EMPTY),
            Arguments.of("BL", false, "Contact Type  is invalid"),
            Arguments.of(StringUtils.EMPTY, false, "All contacts must select a role.")
        );
  }

}