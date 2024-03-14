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

/**
 * This class is responsible for converting SubmissionMatchDetailEntity before and after it is saved in the database.
 * It implements BeforeConvertCallback and AfterConvertCallback interfaces.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionMatchDetailEntityBeforeConvert
    implements BeforeConvertCallback<SubmissionMatchDetailEntity>,
    AfterConvertCallback<SubmissionMatchDetailEntity> {

  // ObjectMapper provides functionality for reading and writing JSON
  private final ObjectMapper mapper;

  /**
   * This method is called before the entity is saved in the database.
   * It converts the matchers field of the entity to a JSON string.
   *
   * @param entity The entity that is about to be saved in the database.
   * @param table The table where the entity will be saved.
   * @return The entity with the converted matchers field.
   */
  @Override
  public Publisher<SubmissionMatchDetailEntity> onBeforeConvert(
      @NonNull SubmissionMatchDetailEntity entity,
      @NonNull SqlIdentifier table
  ) {
    return Mono.justOrEmpty(entity.withMatchingField(convertTo(entity)));
  }

  /**
   * This method is called after the entity is retrieved from the database.
   * It converts the matchingField field of the entity from a JSON string to a Map.
   *
   * @param entity The entity that was retrieved from the database.
   * @param table The table where the entity was retrieved from.
   * @return The entity with the converted matchingField field.
   */
  @Override
  public Publisher<SubmissionMatchDetailEntity> onAfterConvert(
      @NonNull SubmissionMatchDetailEntity entity,
      @NonNull SqlIdentifier table) {
    return Mono.justOrEmpty(entity.withMatchers(convertFrom(entity)));
  }

  /**
   * This method converts the matchers field of the entity to a JSON string.
   *
   * @param entity The entity whose matchers field will be converted.
   * @return The JSON string representation of the matchers field.
   */
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

  /**
   * This method converts the matchingField field of the entity from a JSON string to a Map.
   *
   * @param entity The entity whose matchingField field will be converted.
   * @return The Map representation of the matchingField field.
   */
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