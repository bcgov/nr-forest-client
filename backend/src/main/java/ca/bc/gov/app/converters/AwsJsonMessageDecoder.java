package ca.bc.gov.app.converters;

import ca.bc.gov.app.dto.cognito.RefreshRequestDto;
import ca.bc.gov.app.dto.cognito.RefreshResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AwsJsonMessageDecoder implements Decoder<RefreshResponseDto> {

  private final ObjectMapper objectMapper;

  @Override
  public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
    return elementType.toClass() == RefreshResponseDto.class
           && mimeType.toString().equals("application/x-amz-json-1.1");
  }

  @Override
  public Flux<RefreshResponseDto> decode(
      Publisher<DataBuffer> inputStream,
      ResolvableType elementType,
      MimeType mimeType,
      Map<String, Object> hints) {
    return Flux
        .from(inputStream)
        .flatMap(dataBuffer -> {
          try {
            return Mono.justOrEmpty(
                objectMapper.readValue(dataBuffer.asInputStream(), RefreshResponseDto.class)
            );
          } catch (IOException e) {
            return Mono.error(new DecodingException("Failed to decode RefreshRequestDto", e));
          }
        });
  }

  @Override
  public Mono<RefreshResponseDto> decodeToMono(
      Publisher<DataBuffer> inputStream,
      ResolvableType elementType,
      MimeType mimeType,
      Map<String, Object> hints
  ) {
    return decode(inputStream, elementType, mimeType, hints).singleOrEmpty();
  }

  @Override
  public List<MimeType> getDecodableMimeTypes() {
    return List.of(MimeType.valueOf("application/x-amz-json-1.1"));
  }
}
