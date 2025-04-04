package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@RegisterReflectionForBinding({
    ClientNameCodeDto.class,
    ForestClientDto.class,
    AddressSearchDto.class,
    ContactSearchDto.class,
    ClientDoingBusinessAsDto.class,
    ClientNameCodeDto.class,
    ForestClientContactDto.class,
    ForestClientLocationDto.class,
    PredictiveSearchResultDto.class,
    JsonPatch.class,
    JsonNode.class
})
public class GlobalServiceConfiguration {

  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.build();
  }

}
