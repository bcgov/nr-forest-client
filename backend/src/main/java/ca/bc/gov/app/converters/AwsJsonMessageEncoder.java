package ca.bc.gov.app.converters;

import ca.bc.gov.app.dto.cognito.RefreshRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AwsJsonMessageEncoder implements Encoder<RefreshRequestDto> {

  private final ObjectMapper objectMapper;


  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    return elementType.toClass() == RefreshRequestDto.class
           && mimeType.toString().equals("application/x-amz-json-1.1");
  }

  @Override
  public Flux<DataBuffer> encode(
      @NotNull Publisher<? extends RefreshRequestDto> inputStream,
      @NotNull DataBufferFactory bufferFactory,
      @NotNull ResolvableType elementType,
      MimeType mimeType,
      Map<String, Object> hints
  ) {
    return Flux.from(inputStream)
        .flatMap(object -> {
          try {
            return Mono.justOrEmpty(bufferFactory.wrap(objectMapper.writeValueAsBytes(object)));
          } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to serialize object to JSON", e));
          }
        });
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return List.of(MimeType.valueOf("application/x-amz-json-1.1"));
  }
}
