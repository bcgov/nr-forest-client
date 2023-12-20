package ca.bc.gov.app.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Unit Test | Forest Client Contact Mapper")
class ForestClientContactMapperTest {


  private final ForestClientContactMapper mapper = Mappers.getMapper(
      ForestClientContactMapper.class);

  ForestClientContactDto dto =  new ForestClientContactDto(
      "00000001",
      "00",
      "BL",
      "James",
      "2502502550",
      "mail@mail.ca",
      "Test",
      "Test",
      1L
  );

  ForestClientContactEntity entity =
      ForestClientContactEntity
          .builder()
          .clientNumber("00000001")
          .clientLocnCode("00")
          .contactCode("BL")
          .contactName("James")
          .businessPhone("2502502550")
          .emailAddress("mail@mail.ca")
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