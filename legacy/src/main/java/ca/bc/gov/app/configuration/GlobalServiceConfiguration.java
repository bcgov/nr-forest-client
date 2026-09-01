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
import com.flipkart.zjsonpatch.Jackson3JsonPatch;
import java.util.Optional;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.http.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

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
    Jackson3JsonPatch.class,
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
    return JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
  }

  @Bean
  CodecCustomizer jacksonCodecCustomizer(ObjectMapper objectMapper) {
    return configurer -> {
      var codecs = configurer.defaultCodecs();
      codecs.jacksonJsonEncoder(new JacksonJsonEncoder((JsonMapper) objectMapper));
      codecs.jacksonJsonDecoder(new JacksonJsonDecoder((JsonMapper) objectMapper));
    };
  }

  @Bean
  R2dbcMappingContext r2dbcMappingContext(Optional<NamingStrategy> namingStrategy) {
    return R2dbcMappingContext.forPlainIdentifiers(
        namingStrategy.orElse(DefaultNamingStrategy.INSTANCE));
  }

}
