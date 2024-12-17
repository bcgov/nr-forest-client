package ca.bc.gov.app.converters;

import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForestClientDetailsSerializerModifier extends BeanSerializerModifier {

  @Override
  public JsonSerializer<?> modifySerializer(
      SerializationConfig config,
      BeanDescription beanDesc,
      JsonSerializer<?> serializer
  ) {

    if (ForestClientDetailsDto.class.isAssignableFrom(beanDesc.getBeanClass())) {
      return new ForestClientObfuscate<>();
    }

    return super.modifySerializer(config, beanDesc, serializer);
  }
}
