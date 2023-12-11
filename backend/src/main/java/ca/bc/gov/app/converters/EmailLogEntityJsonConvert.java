package ca.bc.gov.app.converters;

import ca.bc.gov.app.entity.client.EmailLogEntity;
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
public class EmailLogEntityJsonConvert
    implements BeforeConvertCallback<EmailLogEntity>,
    AfterConvertCallback<EmailLogEntity> {

  private final ObjectMapper mapper;

  @Override
  public Publisher<EmailLogEntity> onBeforeConvert(
      @NonNull EmailLogEntity entity,
      @NonNull SqlIdentifier table
  ) {
    return Mono.justOrEmpty(entity.withEmailVariables(convertTo(entity)));
  }

  @Override
  public Publisher<EmailLogEntity> onAfterConvert(
      @NonNull EmailLogEntity entity,
      @NonNull SqlIdentifier table) {
    return Mono.justOrEmpty(entity.withVariables(convertFrom(entity)));
  }

  private Json convertTo(EmailLogEntity entity) {
    return Json
        .of(
            Optional
                .ofNullable(entity.getVariables())
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
  private Map<String, Object> convertFrom(EmailLogEntity entity) {
    return
        Optional
            .ofNullable(entity.getEmailVariables())
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