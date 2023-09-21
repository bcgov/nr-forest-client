package ca.bc.gov.app.converters;


import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.r2dbc.postgresql.codec.Json;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionMatchDetailEntityBeforeConvert
    implements BeforeConvertCallback<SubmissionMatchDetailEntity>,
    AfterConvertCallback<SubmissionMatchDetailEntity> {

  private final Jackson2ObjectMapperBuilder builder;

  @Override
  public Publisher<SubmissionMatchDetailEntity> onBeforeConvert(
      SubmissionMatchDetailEntity entity,
      SqlIdentifier table
  ) {
    log.info("Converting to");
    return Mono.justOrEmpty(entity.withMatchingField(convertTo(entity)));
  }

  @Override
  public Publisher<SubmissionMatchDetailEntity> onAfterConvert(SubmissionMatchDetailEntity entity,
      SqlIdentifier table) {
    log.info("Converting from");
    return Mono.justOrEmpty(entity.withMatchers(convertFrom(entity)));
  }

  private Json convertTo(SubmissionMatchDetailEntity entity) {
    String json = "{}";

    try {
      json = builder
          .build()
          .writeValueAsString(entity.getMatchers());
    } catch (JsonProcessingException e) {
      log.error("Error while converting matchers to json", e);
    }

    return Json.of(json);
  }

  private Map<String, Object> convertFrom(SubmissionMatchDetailEntity entity) {
    String json = StringUtils.defaultString(entity.getMatchingField().asString(), "{}");

    try {
      return builder.build().readValue(json, Map.class);
    } catch (JsonProcessingException e) {
      log.error("Error while converting matchers to json", e);
    }

    return Map.of();
  }
}