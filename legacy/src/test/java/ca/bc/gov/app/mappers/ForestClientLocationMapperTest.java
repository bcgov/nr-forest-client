package ca.bc.gov.app.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Unit Test | Forest Client Location Mapper")
class ForestClientLocationMapperTest {

  private final ForestClientLocationMapper mapper = Mappers.getMapper(
      ForestClientLocationMapper.class);

  ForestClientLocationDto dto = new ForestClientLocationDto(
      "00000001",
      "00",
      "BILLING ADDRESS",
      "2975 Jutland Road",
      null,
      null,
      "VICTORIA",
      "BC",
      "V8V8V8",
      "CANADA",
      null,
      null,
      null,
      null,
      null,
      "N",
      null,
      "Y",
      null,
      "Test",
      "Test",
      1L
  );

  ForestClientLocationEntity entity =
      ForestClientLocationEntity
          .builder()
          .clientNumber("00000001")
          .clientLocnCode("00")
          .clientLocnName("BILLING ADDRESS")
          .addressOne("2975 Jutland Road")
          .hdbsCompanyCode(" ")
          .city("VICTORIA")
          .province("BC")
          .postalCode("V8V8V8")
          .country("CANADA")
          .locnExpiredInd("N")
          .trustLocationInd("Y")
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