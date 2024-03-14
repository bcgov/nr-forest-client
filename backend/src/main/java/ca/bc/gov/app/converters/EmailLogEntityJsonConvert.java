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

/**
 * This class is responsible for converting EmailLogEntity objects to and from JSON format.
 * It implements the BeforeConvertCallback and AfterConvertCallback interfaces from Spring Data R2DBC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailLogEntityJsonConvert
    implements BeforeConvertCallback<EmailLogEntity>,
    AfterConvertCallback<EmailLogEntity> {

  // ObjectMapper is used for converting Java objects to and from JSON.
  private final ObjectMapper mapper;

  /**
   * This method is called before the conversion of an EmailLogEntity object.
   * It converts the entity's variables to JSON format.
   *
   * @param entity The EmailLogEntity object to be converted.
   * @param table The SQL table identifier.
   * @return A Publisher that emits the converted EmailLogEntity object.
   */
  @Override
  public Publisher<EmailLogEntity> onBeforeConvert(
      @NonNull EmailLogEntity entity,
      @NonNull SqlIdentifier table
  ) {
    return Mono.justOrEmpty(entity.withEmailVariables(convertTo(entity)));
  }

  /**
   * This method is called after the conversion of an EmailLogEntity object.
   * It converts the entity's variables from JSON format to a Map.
   *
   * @param entity The EmailLogEntity object that was converted.
   * @param table The SQL table identifier.
   * @return A Publisher that emits the converted EmailLogEntity object.
   */
  @Override
  public Publisher<EmailLogEntity> onAfterConvert(
      @NonNull EmailLogEntity entity,
      @NonNull SqlIdentifier table) {
    return Mono.justOrEmpty(entity.withVariables(convertFrom(entity)));
  }

  /**
   * This method converts the variables of an EmailLogEntity object to JSON format.
   *
   * @param entity The EmailLogEntity object whose variables are to be converted.
   * @return The converted variables in JSON format.
   */
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

  /**
   * This method converts the variables of an EmailLogEntity object from JSON format to a Map.
   *
   * @param entity The EmailLogEntity object whose variables are to be converted.
   * @return The converted variables as a Map.
   */
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