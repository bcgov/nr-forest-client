package ca.bc.gov.app.converters;

import static ca.bc.gov.app.TestConstants.COGNITO_DTO;
import static ca.bc.gov.app.TestConstants.COGNITO_REFRESH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.dto.cognito.RefreshRequestDto;
import ca.bc.gov.app.dto.cognito.RefreshResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | AwsJsonMessageDecoder")
class AwsJsonMessageDecoderTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AwsJsonMessageDecoder sut = new AwsJsonMessageDecoder(objectMapper);

  private final DataBuffer buffer = new DefaultDataBufferFactory()
      .wrap(COGNITO_REFRESH.getBytes(StandardCharsets.UTF_8));

  @Test
  void shouldGetDecodableMime() {
    assertEquals(
        List.of(MimeType.valueOf("application/x-amz-json-1.1")),
        sut.getDecodableMimeTypes()
    );
  }

  @Test
  void shouldBeAbleToDecode() {
    assertTrue(
        sut.canDecode(
            ResolvableType.forType(RefreshResponseDto.class),
            MimeType.valueOf("application/x-amz-json-1.1")
        )
    );
  }

  @Test
  void shouldDecodeToFlux() {
    sut.decode(
            Mono.just(buffer),
            ResolvableType.forType(RefreshRequestDto.class),
            MimeType.valueOf("application/x-amz-json-1.1"),
            null
        )
        .as(StepVerifier::create)
        .expectNext(COGNITO_DTO)
        .verifyComplete();
  }

  @Test
  void shouldDecodeToMono() {
    sut.decodeToMono(
            Mono.just(buffer),
            ResolvableType.forType(RefreshRequestDto.class),
            MimeType.valueOf("application/x-amz-json-1.1"),
            null
        )
        .as(StepVerifier::create)
        .expectNext(COGNITO_DTO)
        .verifyComplete();
  }

}