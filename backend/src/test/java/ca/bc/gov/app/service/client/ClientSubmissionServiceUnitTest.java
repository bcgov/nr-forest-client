package ca.bc.gov.app.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Unit Test | Client Submission Service")
class ClientSubmissionServiceUnitTest {

  private ClientSubmissionService service;

  @BeforeEach
  void setUp() {
    service = new ClientSubmissionService(
        null, null, null, null, null, null, null, null, null, null, null, null
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("Should return empty string when input is null, empty or blank")
  void shouldReturnEmptyStringWhenInputIsBlank(String input) {
    assertEquals("", service.removeProvider(input));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "IDIR\\SLEROY, SLEROY",
      "BCEIDBUSINESS\\Mitchbrown-john, Mitchbrown-john",
      "BCSC\\user123, user123",
      "no_provider_user, no_provider_user"
  })
  @DisplayName("Should remove provider prefix when present")
  void shouldRemoveProviderPrefix(String input, String expected) {
    assertEquals(expected, service.removeProvider(input));
  }

}

