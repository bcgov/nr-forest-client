package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAlternateNameDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDetailsDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Service")
class ClientServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientService service;

  @MockitoBean
  private ClientLegacyService legacyService;

  @MockitoBean
  private BcRegistryService bcRegistryService;
  

  @Test
  @DisplayName("Search by incorporation number and fail due to no legal type")
  void shouldSearchByIncorporationAndFail1() {

    String corpRegnNmbr = "C00123456";
    String userId = "idir\\jwick";
    String businessId = "123456";

    BcRegistryOfficerDto mockOfficer = new BcRegistryOfficerDto(
        "officer@email.com",
        "John",
        "Doe",
        "D",
        "123456",
        "My Company Ltd.",
        "Person");

    BcRegistryAddressDto mockAddress = new BcRegistryAddressDto(
        "City",
        "Canada",
        "BC",
        "A1B2C3",
        "Street",
        "",
        "",
        "");

    BcRegistryRoleDto mockRole = new BcRegistryRoleDto(
        LocalDate.now().minusYears(1),
        null,
        "Owner");

    BcRegistryPartyDto mockParty = new BcRegistryPartyDto(
        mockAddress,
        mockAddress,
        mockOfficer,
        List.of(mockRole));

    BcRegistryBusinessAdressesDto mockBusinessOffice = new BcRegistryBusinessAdressesDto(
        mockAddress,
        mockAddress);

    BcRegistryAlternateNameDto mockAlternateName = new BcRegistryAlternateNameDto(
        "EntityType",
        corpRegnNmbr,
        "Alternate Name",
        ZonedDateTime.now(),
        LocalDate.now());

    BcRegistryBusinessDto mockBusinessDto = new BcRegistryBusinessDto(
        List.of(mockAlternateName),
        true,
        false,
        false,
        false,
        corpRegnNmbr,
        "MY COMPANY LTD.",
        "TOTS",
        "Active",
        null);

    BcRegistryOfficesDto mockOffices = new BcRegistryOfficesDto(mockBusinessOffice);

    BcRegistryDocumentDto mockDocumentDto =
        new BcRegistryDocumentDto(mockBusinessDto, mockOffices, List.of(mockParty));

    Mockito
        .when(bcRegistryService
            .requestDocumentData(corpRegnNmbr))
        .thenReturn(Flux.just(mockDocumentDto));

    Mockito
        .when(legacyService
            .searchLegacy(corpRegnNmbr,"Alternate Name",userId,businessId))
            .thenReturn(Flux.empty());

    service.getClientDetailsByIncorporationNumber(corpRegnNmbr, userId, businessId,"idir")
      .as(StepVerifier::create)
        .expectErrorMessage("406 NOT_ACCEPTABLE \"Unsupported Legal Type: TOTS\"")
      .verify();

  }

  @Test
  @DisplayName("Search by client number and succeed")
  void shouldSearchByClientNumberAndGetResult() {

    String clientNumber = "00123456";
    String corpRegnNmbr = "C00123456";

    ForestClientDetailsDto clientDto = new ForestClientDetailsDto(
        new ForestClientInformationDto(clientNumber,
            "MY COMPANY LTD.",
            null,
            null,
            "ACT",
            "Active",
            "C",
            "Corporation",
            "ID",
            "Client Identification",
            "00123456",
            "B",
            corpRegnNmbr,
            "MYCO",
            "678",
            "Test Client",
            LocalDateTime.of(2021, 1, 1, 0, 0, 0),
            "Admin",
            null,
            null),
        null,
        null,
        null,
        Map.of()
    );

    Mockito
        .when(legacyService
            .searchByClientNumber(clientNumber))
        .thenReturn(Mono.just(clientDto));

    Mockito
        .when(legacyService
            .getRelatedClientList(clientNumber))
        .thenReturn(Mono.empty());

    service.getClientDetailsByClientNumber(clientNumber)
        .as(StepVerifier::create)
        .expectNext(clientDto)
        .verifyComplete();
  }

  @Test
  @DisplayName("Search by client number and succeed including dependencies")
  void shouldSearchByClientNumberAndGetResultWithContactAndLocation() {

    String clientNumber = "00123456";
    String corpRegnNmbr = "C00123456";

    ForestClientDetailsDto clientDto = new ForestClientDetailsDto(
        new ForestClientInformationDto(clientNumber,
            "MY COMPANY LTD.",
            null,
            null,
            "ACT",
            "Active",
            "C",
            "Corporation",
            "ID",
            "Client Identification",
            "00123456",
            "B",
            corpRegnNmbr,
            "MYCO",
            "678",
            "Test Client",
            LocalDateTime.of(2021, 1, 1, 0, 0, 0),
            "Admin",
            null,
            null),
        List.of(
            new ForestClientLocationDetailsDto(
                clientNumber,
                "00",
                "Location",
                "1234 Street",
                null,
                null,
                "City",
                "BC",
                "British Columbia",
                "CA",
                "Canada",
                "A1B2C3",
                "1234567890",
                "0987654321",
                "1234567890",
                "0987654321",
                "mail@notme.ca",
                "N",
                null
            )
        ),
        List.of(
            new ForestClientContactDetailsDto(
                clientNumber,
                1L,
                "John Doe",
                "DI",
                "Director",
                "1234567890",
                "0987654321",
                "1234567890",
                "mail@nobody.ca",
                List.of("00")
            )

        ),
        null,
        Map.of()
    );

    Mockito
        .when(legacyService
            .searchByClientNumber(clientNumber))
        .thenReturn(Mono.just(clientDto));

    Mockito
        .when(legacyService
            .getRelatedClientList(clientNumber))
        .thenReturn(Mono.empty());

    service.getClientDetailsByClientNumber(clientNumber)
        .as(StepVerifier::create)
        .expectNext(clientDto.withClient(clientDto.client()))
        .verifyComplete();
  }
  
  @Test
  @DisplayName("Should return 'Y' for client with good standing in BC Registry")
  void shouldReturnYWhenClientIsInGoodStanding() {
    String clientNumber = "00123456";
    String corpRegnNmbr = "C00123456";
    String registryCompanyTypeCode = "B";

    ForestClientDetailsDto clientDto = new ForestClientDetailsDto(
        new ForestClientInformationDto(
            clientNumber,
            "MY COMPANY LTD.",
            null,
            null,
            "ACT",
            "Active",
            "C",
            "Corporation",
            "ID",
            "Client Identification",
            clientNumber,
            registryCompanyTypeCode,
            corpRegnNmbr,
            "MYCO",
            "678",
            "Test Client",
            LocalDateTime.of(2021, 1, 1, 0, 0),
            "Admin",
            null,
            null
        ),
        null,
        null,
        null,
        Map.of()
    );

    BcRegistryBusinessDto businessDto = new BcRegistryBusinessDto(
        List.of(),
        true,
        false,
        false,
        false,
        corpRegnNmbr,
        "MY COMPANY LTD.",
        "LIC",
        "Active",
        null
    );

    BcRegistryDocumentDto documentDto = new BcRegistryDocumentDto(
        businessDto,
        null,
        null
    );

    Mockito.when(legacyService.searchByClientNumber(clientNumber))
        .thenReturn(Mono.just(clientDto));

    Mockito.when(bcRegistryService.requestDocumentData("B" + corpRegnNmbr))
        .thenReturn(Flux.just(documentDto));

    service.getGoodStandingIndicator(clientNumber)
        .as(StepVerifier::create)
        .expectNext("Y")
        .verifyComplete();
  }

  @Test
  @DisplayName("Get BC Registry information and return document data")
  void shouldGetBcRegistryInformationSuccessfully() {

    String registrationNumber = "BC0123456";

    BcRegistryBusinessDto mockBusinessDto = new BcRegistryBusinessDto(
        List.of(),
        true,
        false,
        false,
        false,
        registrationNumber,
        "TEST COMPANY LTD.",
        "BC",
        "Active",
        null
    );

    BcRegistryAddressDto mockAddress = new BcRegistryAddressDto(
        "Victoria",
        "Canada",
        "BC",
        "V8V1X4",
        "501 Belleville Street",
        "",
        "",
        ""
    );

    BcRegistryBusinessAdressesDto mockBusinessOffice = new BcRegistryBusinessAdressesDto(
        mockAddress,
        mockAddress
    );

    BcRegistryOfficesDto mockOffices = new BcRegistryOfficesDto(mockBusinessOffice);

    BcRegistryOfficerDto mockOfficer = new BcRegistryOfficerDto(
        "officer@email.com",
        "John",
        "Doe",
        "D",
        "123456",
        "Test Company Ltd.",
        "Person"
    );

    BcRegistryRoleDto mockRole = new BcRegistryRoleDto(
        LocalDate.now().minusYears(1),
        null,
        "Director"
    );

    BcRegistryPartyDto mockParty = new BcRegistryPartyDto(
        mockAddress,
        mockAddress,
        mockOfficer,
        List.of(mockRole)
    );

    BcRegistryDocumentDto mockDocumentDto = new BcRegistryDocumentDto(
        mockBusinessDto,
        mockOffices,
        List.of(mockParty)
    );

    Mockito
        .when(bcRegistryService.requestDocumentData(registrationNumber))
        .thenReturn(Flux.just(mockDocumentDto));

    service.getBcRegistryInformation(registrationNumber)
        .as(StepVerifier::create)
        .expectNext(mockDocumentDto)
        .verifyComplete();
  }

  @Test
  @DisplayName("Get BC Registry information with empty result")
  void shouldGetBcRegistryInformationEmpty() {

    String registrationNumber = "BC9999999";

    Mockito
        .when(bcRegistryService.requestDocumentData(registrationNumber))
        .thenReturn(Flux.empty());

    service.getBcRegistryInformation(registrationNumber)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  @DisplayName("Get BC Registry information with error from service")
  void shouldGetBcRegistryInformationError() {

    String registrationNumber = "INVALID";

    Mockito
        .when(bcRegistryService.requestDocumentData(registrationNumber))
        .thenReturn(Flux.error(new RuntimeException("BC Registry service unavailable")));

    service.getBcRegistryInformation(registrationNumber)
        .as(StepVerifier::create)
        .expectErrorMessage("BC Registry service unavailable")
        .verify();
  }

  @Test
  @DisplayName("Get BC Registry information with multiple documents")
  void shouldGetBcRegistryInformationMultipleDocuments() {

    String registrationNumber = "FM0012345";

    BcRegistryBusinessDto mockBusinessDto1 = new BcRegistryBusinessDto(
        List.of(),
        true,
        false,
        false,
        false,
        registrationNumber,
        "FIRST COMPANY LTD.",
        "SP",
        "Active",
        null
    );

    BcRegistryBusinessDto mockBusinessDto2 = new BcRegistryBusinessDto(
        List.of(),
        false,
        false,
        false,
        false,
        registrationNumber,
        "SECOND COMPANY LTD.",
        "GP",
        "Active",
        null
    );

    BcRegistryDocumentDto doc1 = new BcRegistryDocumentDto(mockBusinessDto1, null, List.of());
    BcRegistryDocumentDto doc2 = new BcRegistryDocumentDto(mockBusinessDto2, null, List.of());

    Mockito
        .when(bcRegistryService.requestDocumentData(registrationNumber))
        .thenReturn(Flux.just(doc1, doc2));

    service.getBcRegistryInformation(registrationNumber)
        .as(StepVerifier::create)
        .expectNext(doc1)
        .expectNext(doc2)
        .verifyComplete();
  }

}
