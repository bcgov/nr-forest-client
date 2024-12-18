package ca.bc.gov.app.converters;

import ca.bc.gov.app.ApplicationConstant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * Requirement - ID - Suppress GIVEN Client detail summary edit mode WHEN in edit mode and role is
 * CLIENT_VIEW THEN system will display the ID using the following format IF ( ID IS NOT BC Registry
 * OR BC Service Card) THEN (example British Columbia Drivers License) { IF ( length < 7 values)
 * THEN display the entire values ELSE [first value]***[Last three values] Example "1***756" END IF
 * } ELSE IF ( BC Registry) Display the entire value. Example "FM232109" ELSE IF (BC Service Card)
 * Display "BC Service card verified" END IF
 * <p>
 * Requirement - Birthday GIVEN Client detail summary edit mode WHEN in edit mode and role is
 * CLIENT_VIEW THEN system will only show year of birth year. Example "1983" AND Column name is
 * "Year of birth" as oppose to "Date of birth"
 */
@Slf4j
public class ForestClientObfuscate<T> extends JsonSerializer<T> {

  private final List<String> obfuscableFields = List.of("clientIdentification", "birthdate");

  @SneakyThrows
  @Override
  public void serialize(
      T value,
      JsonGenerator gen,
      SerializerProvider provider
  ) throws IOException {

    if (value == null) {
      gen.writeNull();
    }

    gen.writeStartObject();

    var beanProps = provider.getConfig().introspect(provider.constructType(value.getClass()))
        .findProperties();

    String clientIdTypeCode = null;

    for (var property : beanProps) {
      var propName = property.getName();
      var rawValue = property.getAccessor().getValue(value);

      // Use the default serializer for other fields
      gen.writeFieldName(propName);

      // Skip null values entirely
      if (rawValue == null) {
        gen.writeNull();
      } else {
        // Client identification type retrieval, hopefully this is the field comes before the value
        if ("clientIdTypeCode".equals(propName)) {
          clientIdTypeCode = rawValue.toString();
        }

        if (obfuscableFields.contains(propName)) {
          gen.writeString(obfuscate(propName, clientIdTypeCode, rawValue));
          continue;
        }

        var serializer = provider.findValueSerializer(property.getRawPrimaryType());
        serializer.serialize(rawValue, gen, provider);
      }
    }

    gen.writeEndObject();
  }

  private String obfuscate(String propName, String propType, Object value) {
    Set<String> roles = toRoles(MDC.get(ApplicationConstant.MDC_USERROLES));

    // Admins and Editors can see with no restrictions
    if (
        roles.contains(ApplicationConstant.ROLE_EDITOR)
        || roles.contains(ApplicationConstant.ROLE_ADMIN)
        || roles.contains(ApplicationConstant.ROLE_SUSPEND)
    ) {
      return value.toString();
    }

    // BC Services card uses a UUID, so we just say it is verified
    if ("clientIdentification".equals(propName) && "BCSC".equals(propType)) {
      return "BC Service card verified";
    }

    if ("clientIdentification".equals(propName) && "BCRE".equals(propType)) {
      return value.toString();
    }

    if ("clientIdentification".equals(propName)) {
      return obfuscateClientIdentification(value.toString());
    }

    if ("birthdate".equals(propName)) {
      return obfuscateBirthdate((LocalDate) value);
    }

    return value.toString();
  }

  private String obfuscateBirthdate(LocalDate value) {
    return String.format("%d-**-**", value.getYear());
  }

  private String obfuscateClientIdentification(String value) {
    if (value.length() < 7) {
      return value;
    }

    return value.charAt(0) + "***" + value.substring(value.length() - 3);
  }

  private Set<String> toRoles(String roleCsv) {
    if (StringUtils.isNotBlank(roleCsv)) {
      return Set.of(roleCsv.split(","));
    }
    return Set.of();
  }

}
