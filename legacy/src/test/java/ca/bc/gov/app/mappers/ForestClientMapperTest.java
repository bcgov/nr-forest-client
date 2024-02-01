package ca.bc.gov.app.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Unit Test | Forest Client Mapper")
class ForestClientMapperTest {

  private final ForestClientMapper mapper = Mappers.getMapper(ForestClientMapper.class);

  ForestClientDto dto = new ForestClientDto(
      "00000001",
      "Test Client",
      null,
      null,
      "ACT",
      "C",
      null,
      null,
      null,
      "BC",
      "0101141401",
      "Client is Corporation",
      "Test",
      "Test",
      1L
  );

  ForestClientEntity entity =
      ForestClientEntity
          .builder()
          .clientNumber("00000001")
          .clientName("Test Client")
          .clientStatusCode("ACT")
          .clientTypeCode("C")
          .registryCompanyTypeCode("BC")
          .corpRegnNmbr("0101141401")
          .wcbFirmNumber(" ")
          .clientComment("Client is Corporation")
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .createdBy("Test")
          .updatedBy("Test")
          .createdByUnit(1L)
          .updatedByUnit(1L)
          .revision(1L)
          .build();

  @Test
  @DisplayName("Should convert to entity")
  void shouldConvertToEntity() {
    assertThat(mapper.toEntity(dto))
        .usingRecursiveComparison()
        .ignoringFields("createdAt", "updatedAt")
        .isEqualTo(entity);

    assertNotNull(mapper.toEntity(dto).getCreatedAt());
    assertNotNull(mapper.toEntity(dto).getUpdatedAt());
  }

  @Test
  @DisplayName("Should convert to dto")
  void shouldConvertToDto() {
    assertEquals(dto, mapper.toDto(entity));
  }

}