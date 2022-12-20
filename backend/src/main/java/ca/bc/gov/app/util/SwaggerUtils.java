package ca.bc.gov.app.util;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.header.Builder.headerBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SwaggerUtils {

  public static <T> Consumer<Builder> buildApi(
      String operation,
      String description,
      String tag,
      Class<T> tClass
  ) {
    return ops ->
        ops.tag(tag)
            .beanClass(tClass)
            .description(description)
            .beanMethod(operation)
            .operationId(operation);
  }

  public static org.springdoc.core.fn.builders.apiresponse.Builder responseOps(
      String responseCode,
      String description,
      org.springdoc.core.fn.builders.content.Builder schema) {
    return responseBuilder().responseCode(responseCode).description(description).content(schema);
  }

  public static Consumer<Builder> badRequestOps(String example) {
    return ops -> ops.response(responseOps(
        "400",
        "Bad Request, Validation failed",
        contentBuilder().schema(SwaggerUtils.classSchema(String.class, example)))
    );
  }

  public static Consumer<Builder> createdOps(
      String exampleSchema
  ) {
    return ops -> ops.response(responseOps(
            "201",
            "Created",
            contentBuilder().schema(schemaBuilder())
        )
            .header(
                headerBuilder()
                    .name("Location")
                    .description("The location header pointing out to the content created")
                    .schema(schemaBuilder().implementation(String.class).example(exampleSchema)))
    );
  }

  public static <T> org.springdoc.core.fn.builders.schema.Builder classSchema(
      Class<T> tClass, String example) {
    return schemaBuilder().implementation(tClass).example(example);
  }

  public static <T> Consumer<Builder> requestBodyOps(Class<T> tClass) {
    return ops -> ops
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder()
                                .implementation(tClass)
                        )
                )
        );
  }

}
