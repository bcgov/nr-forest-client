package ca.bc.gov.app.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

@DisplayName("Unit Test | Forest Client Obfuscate Serializer")
class ForestClientObfuscateTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  @BeforeAll
  public static void setUp() {
    SimpleModule module = new SimpleModule();
    module.setSerializerModifier(new ForestClientDetailsSerializerModifier());
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(module);

  }

  @Test
  @DisplayName("should obfuscate identification and birthdate for viewers")
  void shouldObfuscateIdentification() throws Exception {
    MDC.put(ApplicationConstant.MDC_USERROLES, ApplicationConstant.ROLE_VIEWER);
    String json = mapper.writeValueAsString(getDto());
    assertThat(json)
        .contains("\"clientIdentification\":\"1***678\"")
        .contains("\"birthdate\":\"1070-**-**\"");
  }

  @Test
  @DisplayName("should obfuscate BCSC identification completely for viewers")
  void shouldObfuscateBCSC() throws Exception {
    MDC.put(ApplicationConstant.MDC_USERROLES, ApplicationConstant.ROLE_VIEWER);
    String json = mapper.writeValueAsString(getDto().withClient(getDto().client().withClientIdTypeCode("BCSC")));
    assertThat(json)
        .contains("\"clientIdentification\":\"BC Service card verified\"")
        .contains("\"birthdate\":\"1070-**-**\"");
  }

  @Test
  @DisplayName("should not obfuscate small identifications for viewers")
  void shouldNotObfuscateSmallIds() throws Exception {
    MDC.put(ApplicationConstant.MDC_USERROLES, ApplicationConstant.ROLE_VIEWER);
    String json = mapper.writeValueAsString(getDto().withClient(getDto().client().withClientIdentification("1234")));
    assertThat(json)
        .contains("\"clientIdentification\":\"1234\"")
        .contains("\"birthdate\":\"1070-**-**\"");
  }

  @Test
  @DisplayName("should not obfuscate identification and birthdate for non-viewers")
  void shouldNotObfuscateForNonViewers() throws Exception {
    MDC.put(ApplicationConstant.MDC_USERROLES, ApplicationConstant.ROLE_EDITOR);
    String json = mapper.writeValueAsString(getDto());
    assertThat(json)
        .contains("\"clientIdentification\":\"12345678\"")
        .contains("\"birthdate\":\"1070-12-13T00:00\"");
  }

  @Test
  @DisplayName("should not obfuscate identification and birthdate for editors with viewer role")
  void shouldNotObfuscateEvenIfHasViewer() throws Exception {
    MDC.put(ApplicationConstant.MDC_USERROLES, String.format("%s,%s", ApplicationConstant.ROLE_EDITOR, ApplicationConstant.ROLE_VIEWER));
    String json = mapper.writeValueAsString(getDto());
    assertThat(json)
        .contains("\"clientIdentification\":\"12345678\"")
        .contains("\"birthdate\":\"1070-12-13T00:00\"");
  }

  private ForestClientDetailsDto getDto() {
    return new ForestClientDetailsDto(
        new ForestClientInformationDto(
            "123456789",
            "Wick",
            "Johnathan",
            null,
            "ACT",
            "Active",
            "I",
            "Individual",
            "BCDL",
            "BC Drivers License",
            "12345678",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            LocalDateTime.of(LocalDate.of(1070, 12, 13), LocalTime.MIN)
        ),
        List.of(),
        List.of(),
        null,
        Map.of()
    );
  }

}