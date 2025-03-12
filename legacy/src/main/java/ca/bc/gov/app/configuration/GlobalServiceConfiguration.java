package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.CopyOperation;
import com.github.fge.jsonpatch.DualPathOperation;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.MoveOperation;
import com.github.fge.jsonpatch.PathValueOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.github.fge.jsonpatch.TestOperation;
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
    ReplaceOperation.class,
    RemoveOperation.class,
    MoveOperation.class,
    AddOperation.class,
    CopyOperation.class,
    DualPathOperation.class,
    PathValueOperation.class,
    JsonPatchOperation.class,
    TestOperation.class,
    JsonPointer.class
})
public class GlobalServiceConfiguration {
  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.build();
  }

}
