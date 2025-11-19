package ca.bc.gov.app.converters;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForestClientObfuscate<T> extends JsonSerializer<T> {

  public static final String CLIENT_IDENTIFICATION = "clientIdentification";
  private final List<String> obfuscateFields = List.of(CLIENT_IDENTIFICATION, "birthdate");

  /**
   * Serializes the given value, obfuscating certain fields based on user roles.
   *
   * @param value The value to serialize.
   * @param gen The JSON generator used to write the JSON output.
   * @param provider The serializer provider.
   * @throws IOException If an I/O error occurs.
   */
  @SneakyThrows
  @Override
  public void serialize(
      T value,
      JsonGenerator gen,
      SerializerProvider provider
  ) throws IOException {

    if (value == null) {
      gen.writeNull();
      return;
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

        if (obfuscateFields.contains(propName)) {
          gen.writeString(obfuscate(propName, clientIdTypeCode, rawValue));
          continue;
        }

        var serializer = provider.findValueSerializer(property.getRawPrimaryType());
        serializer.serialize(rawValue, gen, provider);
      }
    }

    gen.writeEndObject();
  }

  /**
   * Obfuscates the given property value based on the property name and type.
   *
   * @param propName The name of the property.
   * @param propType The type of the property.
   * @param value The value to obfuscate.
   * @return The obfuscated value as a string.
   */
  private String obfuscate(String propName, String propType, Object value) {
    Set<String> roles = JwtPrincipalUtil.getRoles();

    // Admins can see the BCSC
    if (
        CLIENT_IDENTIFICATION.equals(propName)
        && "BCSC".equals(propType)
        && roles.contains(ApplicationConstant.ROLE_ADMIN)
    ) {
      return value.toString();
    }
    
    // BC Services card uses a UUID, so we just say it is verified
    if (CLIENT_IDENTIFICATION.equals(propName) && "BCSC".equals(propType)) {
      return "BC Service card verified";
    }

    // Admins and Editors can see with no restrictions
    if (
        roles.contains(ApplicationConstant.ROLE_EDITOR)
        || roles.contains(ApplicationConstant.ROLE_ADMIN)
        || roles.contains(ApplicationConstant.ROLE_SUSPEND)
    ) {
      return value.toString();
    }

    if (CLIENT_IDENTIFICATION.equals(propName) && "BCRE".equals(propType)) {
      return value.toString();
    }

    if (CLIENT_IDENTIFICATION.equals(propName)) {
      return obfuscateClientIdentification(value.toString());
    }

    if ("birthdate".equals(propName)) {
      return obfuscateBirthdate((LocalDateTime) value);
    }

    return value.toString();
  }

  /**
   * Obfuscates the birthdate by masking the day and month.
   *
   * @param value The birthdate to obfuscate.
   * @return The obfuscated birthdate as a string.
   */
  private String obfuscateBirthdate(LocalDateTime value) {
    return String.format("%d-**-**", value.getYear());
  }

  /**
   * Obfuscates the client identification by masking the middle characters.
   *
   * @param value The client identification to obfuscate.
   * @return The obfuscated client identification as a string.
   */
  private String obfuscateClientIdentification(String value) {
    if (value.length() < 7) {
      return value;
    }

    return value.charAt(0) + "***" + value.substring(value.length() - 3);
  }

}