package ca.bc.gov.app.converters;

import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.extern.slf4j.Slf4j;

/**
 * A custom BeanSerializerModifier that modifies the serializer for ForestClientDetailsDto. If the
 * bean class is assignable from ForestClientDetailsDto, it returns a custom serializer
 * ForestClientObfuscate. Otherwise, it returns the default serializer.
 */
@Slf4j
public class ForestClientDetailsSerializerModifier extends BeanSerializerModifier {

  /**
   * Modifies the serializer for the given bean description.
   *
   * @param config     The serialization configuration.
   * @param beanDesc   The bean description.
   * @param serializer The default serializer.
   * @return A custom serializer if the bean class is ForestClientDetailsDto, otherwise the default
   * serializer.
   */
  @Override
  public JsonSerializer<?> modifySerializer(
      SerializationConfig config,
      BeanDescription beanDesc,
      JsonSerializer<?> serializer
  ) {

    if (ForestClientInformationDto.class.isAssignableFrom(beanDesc.getBeanClass())) {
      return new ForestClientObfuscate<>();
    }

    return super.modifySerializer(config, beanDesc, serializer);
  }
}