package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.FieldReasonDto;
import ca.bc.gov.app.dto.ForestClientContactDetailsDto;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.dto.ForestClientDetailsDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.ForestClientInformationDto;
import ca.bc.gov.app.dto.ForestClientLocationDetailsDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.dto.RelatedClientDto;
import ca.bc.gov.app.dto.RelatedClientEntryDto;
import ca.bc.gov.app.entity.ClientRelatedProjection;
import ca.bc.gov.app.entity.RelatedClientEntity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flipkart.zjsonpatch.JsonPatch;
import java.util.Optional;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.http.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

@Configuration
@RegisterReflectionForBinding({
    AddressSearchDto.class,
    ClientDoingBusinessAsDto.class,
    ClientNameCodeDto.class,
    CodeNameDto.class,
    ContactSearchDto.class,
    FieldReasonDto.class,
    ForestClientContactDto.class,
    ForestClientDetailsDto.class,
    ForestClientDto.class,
    ForestClientInformationDto.class,
    ForestClientLocationDetailsDto.class,
    ForestClientLocationDto.class,
    PredictiveSearchResultDto.class,
    JsonPatch.class,
    JsonNode.class,
    ForestClientContactDetailsDto.class,
    ClientRelatedProjection.class,
    RelatedClientEntryDto.class,
    RelatedClientDto.class,
    RelatedClientEntity.class
})
public class GlobalServiceConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Bean
  CodecCustomizer jackson2CodecCustomizer(ObjectMapper objectMapper) {
    return configurer -> {
      var codecs = configurer.defaultCodecs();
      codecs.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
      codecs.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    };
  }

  @Bean
  R2dbcMappingContext r2dbcMappingContext(Optional<NamingStrategy> namingStrategy) {
    return R2dbcMappingContext.forPlainIdentifiers(
        namingStrategy.orElse(DefaultNamingStrategy.INSTANCE));
  }

}
