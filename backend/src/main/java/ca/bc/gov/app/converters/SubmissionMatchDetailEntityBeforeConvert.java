package ca.bc.gov.app.converters;

import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionMatchDetailEntityBeforeConvert
    implements BeforeConvertCallback<SubmissionMatchDetailEntity>,
    AfterConvertCallback<SubmissionMatchDetailEntity> {

  private final ObjectMapper mapper;

  @Override
  public Publisher<SubmissionMatchDetailEntity> onBeforeConvert(
      @NonNull SubmissionMatchDetailEntity entity,
      @NonNull SqlIdentifier table
  ) {
    return Mono.justOrEmpty(entity.withMatchingField(convertTo(entity)));
  }

  @Override
  public Publisher<SubmissionMatchDetailEntity> onAfterConvert(
      @NonNull SubmissionMatchDetailEntity entity,
      @NonNull SqlIdentifier table) {
    return Mono.justOrEmpty(entity.withMatchers(convertFrom(entity)));
  }

  private Json convertTo(SubmissionMatchDetailEntity entity) {
    return Json
        .of(
            Optional
                .ofNullable(entity.getMatchers())
                .map(value -> {
                  try {
                    return mapper.writeValueAsString(value);
                  } catch (JsonProcessingException e) {
                    log.error("Error while converting matchers to json", e);
                    return "{}";
                  }
                })
                .orElse("{}")
        );

  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> convertFrom(SubmissionMatchDetailEntity entity) {
    return Optional
            .ofNullable(entity.getMatchingField())
            .map(Json::asString)
            .map(value -> Objects.toString(value, "{}"))
            .map(value -> {
                try {
                    return mapper.readValue(value, Map.class);
                } catch (JsonProcessingException e) {
                    log.error("Error while converting matchers to json", e);
                    return Map.of();
                }
            })
            .map(value -> (Map<String, Object>) value)
            .orElse(Map.of());
  }
  
}