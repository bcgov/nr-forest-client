package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import ca.bc.gov.app.dto.legacy.PredictiveSearchResultDto;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@RegisterReflectionForBinding({
    CodeNameDto.class,
    ForestClientDto.class,
    AddressSearchDto.class,
    ContactSearchDto.class,
    ClientDoingBusinessAsDto.class,
    ForestClientContactDto.class,
    ForestClientLocationDto.class,
    PredictiveSearchResultDto.class
})
public class GlobalServiceConfiguration {
  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.build();
  }

}
