package ca.bc.gov.app.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Unit Test | Forest Client Doing Business As Mapper")
class ForestClientDoingBusinessAsMapperTest {


  private final ForestClientDoingBusinessAsMapper mapper = Mappers.getMapper(
      ForestClientDoingBusinessAsMapper.class);

  ClientDoingBusinessAsDto dto = new ClientDoingBusinessAsDto(
      "00000001",
      "Spaldingnad",
      "Test",
      "Test",
      1L
  );

  ClientDoingBusinessAsEntity entity =
      ClientDoingBusinessAsEntity
          .builder()
          .clientNumber("00000001")
          .doingBusinessAsName("Spaldingnad")
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