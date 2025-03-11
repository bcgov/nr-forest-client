package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAlternateNameDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDetailsDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Service")
class ClientServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientService service;

  @MockBean
  private ClientLegacyService legacyService;

  @MockBean
  private BcRegistryService bcRegistryService;

  @Test
  @DisplayName("Return client details with good standing indicator derived from BcRegistryDocumentDto")
  void testGetClientDetailsWithGoodStandingIndicator() {
    String clientNumber = "123456";
    String corpRegnNmbr = "9607514";
    LocalDateTime date = LocalDateTime.of(2021, 1, 1, 0, 0, 0);

    ForestClientDetailsDto initialDto = new ForestClientDetailsDto(
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
            "123456789",
            "BC",
            corpRegnNmbr,
            "MYCO",
            "678",
            "Test Client",
            date,
            "Admin",
            null,
            null),
        null, 
        null, 
        null);

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

    BcRegistryAddressDto mockMailingAddress = mockAddress;
    BcRegistryAddressDto mockDeliveryAddress = mockAddress;
    BcRegistryBusinessAdressesDto mockBusinessOffice = new BcRegistryBusinessAdressesDto(
        mockMailingAddress, 
        mockDeliveryAddress);

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
        "Corporation", 
        "Active");

    BcRegistryOfficesDto mockOffices = new BcRegistryOfficesDto(mockBusinessOffice);

    BcRegistryDocumentDto mockDocumentDto =
        new BcRegistryDocumentDto(mockBusinessDto, mockOffices, List.of(mockParty));

    ForestClientDetailsDto expectedDto = new ForestClientDetailsDto(
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
            "123456789",
            "BC",
            corpRegnNmbr,
            "MYCO",
            "678",
            "Test Client",
            date,
            "Admin",
            "Y",
            null
        ),
        null, 
        null, 
        null);

    Mockito
      .when(legacyService.searchByClientNumber(clientNumber))
      .thenReturn(Mono.just(initialDto));

    Mockito
      .when(bcRegistryService
      .requestDocumentData(corpRegnNmbr))
      .thenReturn(Flux.just(mockDocumentDto));

    service
      .getClientDetailsByClientNumber(clientNumber)
      .as(StepVerifier::create)
      .expectNext(expectedDto)
      .verifyComplete();
  }

  @Test
  @DisplayName("Search by incorporation number and fail due to no legal type")
  void shouldSearchByIncorporationAndFail1(){

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
        "Active");

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
  void shouldSearchByClientNumberAndgetResult(){

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
        null);


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
        "LIC",
        "Active");

    BcRegistryOfficesDto mockOffices = new BcRegistryOfficesDto(mockBusinessOffice);

    BcRegistryDocumentDto mockDocumentDto =
        new BcRegistryDocumentDto(mockBusinessDto, mockOffices, List.of(mockParty));

    Mockito
        .when(bcRegistryService
            .requestDocumentData(corpRegnNmbr))
        .thenReturn(Flux.just(mockDocumentDto));

    Mockito
        .when(legacyService
            .searchByClientNumber(clientNumber))
        .thenReturn(Mono.just(clientDto));

    service.getClientDetailsByClientNumber(clientNumber)
        .as(StepVerifier::create)
        .expectNext(clientDto.withClient(clientDto.client().withGoodStandingInd("Y")))
        .verifyComplete();
  }

  @Test
  @DisplayName("Search by client number and succeed including dependencies")
  void shouldSearchByClientNumberAndgetResultWithContactAndLocation(){

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
            new ForestClientContactDto(
                clientNumber,
                "00",
                List.of("00"),
                "DI",
                "Director",
                "John Doe",
                "1234567890",
                "0987654321",
                "1234567890",
                "mail@nobody.ca",
                "Admin",
                "Admin",
                70L
            )

        ),
        null);


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
        "LIC",
        "Active");

    BcRegistryOfficesDto mockOffices = new BcRegistryOfficesDto(mockBusinessOffice);

    BcRegistryDocumentDto mockDocumentDto =
        new BcRegistryDocumentDto(mockBusinessDto, mockOffices, List.of(mockParty));

    Mockito
        .when(bcRegistryService
            .requestDocumentData(corpRegnNmbr))
        .thenReturn(Flux.just(mockDocumentDto));

    Mockito
        .when(legacyService
            .searchByClientNumber(clientNumber))
        .thenReturn(Mono.just(clientDto));

    service.getClientDetailsByClientNumber(clientNumber)
        .as(StepVerifier::create)
        .expectNext(clientDto.withClient(clientDto.client().withGoodStandingInd("Y")))
        .verifyComplete();
  }

}
