package ca.bc.gov.app.controller;

import ca.bc.gov.app.service.ReportingClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(
    name = "Forest Client Reporting",
    description = "Generate reports for the forest client"
)
@RequestMapping(value = "/api/reports", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
@RequiredArgsConstructor
public class ReportingClientController {

  private final ReportingClientService service;

  @GetMapping("/all")
  @Operation(summary = "Get an excel report file for all existing forest clients")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Mono<Void> getAllClientsReport(ServerHttpResponse response) {
    return
        getReport(
            service.generateAllClientsReport(),
            response,
            "All Clients"
        );
  }

  @GetMapping("/businessAs")
  @Operation(summary = "Get a business as excel report file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Mono<Void> getBusinessAsReport(ServerHttpResponse response) {
    return
        getReport(
            service.generateDoingBusinessAsReport(),
            response,
            "Business As"
        );
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
