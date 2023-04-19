package ca.bc.gov.app.controller;

import ca.bc.gov.app.service.ReportingClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(
    name = "Forest Client Reporting",
    description = "Generate reports for the forest client"
)
@RequestMapping(value = "/api/reports")
@RequiredArgsConstructor
public class ReportingClientController {

  private final ReportingClientService service;

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Get an excel report file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
      @ApiResponse(responseCode = "404", description = "No report found for ID 00000000")
  })
  public Mono<Void> getReport(
      @Parameter(name = "id", in = ParameterIn.PATH, description = "ID of the report to be downloaded")
      @PathVariable String id,
      ServerHttpResponse response
  ) {
    return getReport(service.getReportFile(id), response, id);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List Existing Report Files")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "A list of available reports",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              array = @ArraySchema(
                  schema = @Schema(implementation = String.class)
              )
          )
      )
  })
  public Mono<List<String>> listReports() {
    return service.listReports().collectList();
  }

  private Mono<Void> getReport(Mono<File> request, ServerHttpResponse response, String fileName) {
    ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
    zeroCopyResponse.getHeaders()
        .set(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=FC Report " + fileName + ".xlsx");
    zeroCopyResponse.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

    return
        request
            .doOnNext(sheet -> zeroCopyResponse.getHeaders().setContentLength(sheet.length()))
            .flatMap(sheet -> zeroCopyResponse.writeWith(sheet, 0, sheet.length()));
  }

}
