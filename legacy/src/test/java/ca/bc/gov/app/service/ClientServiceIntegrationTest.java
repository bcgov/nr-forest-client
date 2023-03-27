package ca.bc.gov.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | Client Service")
class ClientServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientService service;

  @ParameterizedTest
  @MethodSource("clientListing")
  @DisplayName("List client")
  void shouldListClient(String clientNumber, Consumer<ForestClientDto> validator, int resultSize) {
    service
        .listClientData(clientNumber, 0, 10)
        .as(StepVerifier::create)
        .assertNext(validator)
        .expectNextCount(resultSize - 1)
        .verifyComplete();
  }

  private static Stream<Arguments> clientListing() {
    return
        Stream.of(
            Arguments.of(null, consumer1(), 10),
            Arguments.of("00000001", consumer1(), 1),
            Arguments.of("00000006", consumer2(), 1)
        );
  }

  private static Consumer<ForestClientDto> consumer1() {
    return dto -> {
      assertThat(dto)
          .isNotNull()
          .hasFieldOrPropertyWithValue("clientNumber", "00000001")
          .hasFieldOrPropertyWithValue("clientName", "BAXTER")
          .hasFieldOrPropertyWithValue("clientStatusCode", "ACT")
          .hasFieldOrPropertyWithValue("clientTypeCode", "I")
          .hasFieldOrPropertyWithValue("registryCompanyTypeCode", null)
          .hasFieldOrPropertyWithValue("corpRegnNmbr", "00000001")
          .hasFieldOrPropertyWithValue("clientAcronym", null)
          .hasFieldOrPropertyWithValue("wcbFirmNumber", null)
          .hasFieldOrPropertyWithValue("knowAs", List.of("BAXTER'S FAMILY"));

      assertThat(dto.locations())
          .isNotNull()
          .isNotEmpty()
          .first()
          .hasFieldOrPropertyWithValue("addressOne", "2080 Labieux Rd")
          .hasFieldOrPropertyWithValue("city", "NANAIMO")
          .hasFieldOrPropertyWithValue("province", "BC")
          .hasFieldOrPropertyWithValue("postalCode", "V9T6J9")
          .hasFieldOrPropertyWithValue("country", "CANADA")
          .hasFieldOrPropertyWithValue("homePhone", "8006618773");
    };
  }

  private static Consumer<ForestClientDto> consumer2() {
    return dto -> {
      assertThat(dto)
          .isNotNull()
          .hasFieldOrPropertyWithValue("clientNumber", "00000006")
          .hasFieldOrPropertyWithValue("clientName", "INDIAN CANADA")
          .hasFieldOrPropertyWithValue("clientStatusCode", "ACT")
          .hasFieldOrPropertyWithValue("clientTypeCode", "G")
          .hasFieldOrPropertyWithValue("registryCompanyTypeCode", null)
          .hasFieldOrPropertyWithValue("corpRegnNmbr", null)
          .hasFieldOrPropertyWithValue("clientAcronym", null)
          .hasFieldOrPropertyWithValue("wcbFirmNumber", null)
          .hasFieldOrPropertyWithValue("knowAs", List.of());

      assertThat(dto.locations())
          .isNotNull()
          .isNotEmpty()
          .hasSize(4)
          .first()
          .hasFieldOrPropertyWithValue("name", "MAILING ADDRESS")
          .hasFieldOrPropertyWithValue("addressOne", "300 - 1550 ALBERNI STREET")
          .hasFieldOrPropertyWithValue("city", "VANCOUVER")
          .hasFieldOrPropertyWithValue("province", "BC")
          .hasFieldOrPropertyWithValue("postalCode", "V6G3C5")
          .hasFieldOrPropertyWithValue("country", "CANADA")
          .hasFieldOrPropertyWithValue("homePhone", null);

      assertThat(dto.locations().get(0).contacts())
          .isNotNull()
          .isNotEmpty()
          .hasSize(2)
          .first()
          .hasFieldOrPropertyWithValue("id", 1717L)
          .hasFieldOrPropertyWithValue("locationCode", "00")
          .hasFieldOrPropertyWithValue("contactCode", "BL")
          .hasFieldOrPropertyWithValue("name", "JASON MOMOA")
          .hasFieldOrPropertyWithValue("businessPhone", "6046666755");

    };
  }

}