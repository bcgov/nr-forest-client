package ca.bc.gov.app.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.dto.cognito.RefreshRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | AwsJsonMessageEncoder")
class AwsJsonMessageEncoderTest {

  private final AwsJsonMessageEncoder sut = new AwsJsonMessageEncoder(new ObjectMapper());

  @Test
  @DisplayName("should get mime")
  void shouldGetMime() {
    assertEquals(
        List.of(MimeType.valueOf("application/x-amz-json-1.1")),
        sut.getEncodableMimeTypes()
    );
  }

  @Test
  @DisplayName("should encode")
  void shouldEncode() {
    sut.encode(
            Mono.just(
                new RefreshRequestDto(
                    "username",
                    "refreshToken",
                    Map.of()
                )
            ),
            new DefaultDataBufferFactory(),
            ResolvableType.forType(RefreshRequestDto.class),
            MimeType.valueOf("application/x-amz-json-1.1"),
            null
        )
        .as(StepVerifier::create)
        .assertNext(dataBuffer -> {
          assertEquals(
              "{\"ClientId\":\"username\",\"AuthFlow\":\"refreshToken\",\"AuthParameters\":{}}",
              new String(dataBuffer.asByteBuffer().array())
          );
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("should be able to encode")
  void shouldBeAbleToEncode() {
    assertTrue(
        sut.canEncode(
            ResolvableType.forType(RefreshRequestDto.class),
            MimeType.valueOf("application/x-amz-json-1.1")
        )
    );

  }

}