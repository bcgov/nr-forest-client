package ca.bc.gov.app.converters;

import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * A custom ValueSerializerModifier that modifies the serializer for ForestClientDetailsDto. If the
 * bean class is assignable from ForestClientDetailsDto, it returns a custom serializer
 * ForestClientObfuscate. Otherwise, it returns the default serializer.
 */
@Slf4j
public class ForestClientDetailsSerializerModifier extends ValueSerializerModifier {

  /**
   * Modifies the serializer for the given bean description.
   *
   * @param config     The serialization configuration.
   * @param beanDesc   The bean description supplier.
   * @param serializer The default serializer.
   * @return A custom serializer if the bean class is ForestClientDetailsDto, otherwise the default
   * serializer.
   */
  @Override
  public ValueSerializer<?> modifySerializer(
      SerializationConfig config,
      BeanDescription.Supplier beanDesc,
      ValueSerializer<?> serializer
  ) {

    if (ForestClientInformationDto.class.isAssignableFrom(beanDesc.getBeanClass())) {
      return new ForestClientObfuscate<>();
    }

    return super.modifySerializer(config, beanDesc, serializer);
  }
}