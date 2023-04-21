package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.dto.OrgBookTopicDto;
import ca.bc.gov.app.dto.OrgBookTopicListResponse;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingReportFileException;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.SheetWriter;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ReportingClientService {

  private final ForestClientRepository forestClientRepository;
  private final WebClient orgBookApi;

  public ReportingClientService(ForestClientRepository forestClientRepository,
                                @Qualifier("orgBookApi") WebClient orgBookApi
  ) {
    this.forestClientRepository = forestClientRepository;
    this.orgBookApi = orgBookApi;
  }

  /**
   * Returns a {@link Mono} of {@link File} representing the report file for the given report ID.
   *
   * @param reportId the report ID for which to retrieve the report file
   * @return a {@link Mono} of {@link File} representing the report file for the given report ID
   * @throws MissingReportFileException if the report file does not exist for the given report ID
   */
  public Mono<File> getReportFile(String reportId) {

    File sheetFile = getReportPath(reportId)
        .normalize()
        .toFile();

    if (!sheetFile.exists()) {
      return Mono.error(new MissingReportFileException(reportId));
    }

    return Mono.just(sheetFile);
  }

  /**
   * Returns a {@link Flux} of {@link String} containing the names of reports in the report folder,
   * with the ".xlsx" extension removed.
   *
   * @return a {@link Flux} of {@link String} containing the names of reports in the report folder
   */
  public Flux<String> listReports() {
    try {
      return Flux
          .fromStream(Files.list(getReportFolder()))
          .map(Path::getFileName)
          .map(Path::toFile)
          .map(File::getName)
          .map(name -> name.replace(".xlsx", StringUtils.EMPTY));
    } catch (IOException exception) {
      log.error("Error while cleaning temp folder", exception);
      return Flux.empty();
    }
  }


  public Mono<Void> removeReport(String id) {
    return getReportFile(id)
        .doOnNext(this::deleteReportFile)
        .then();
  }

  @Scheduled(cron = "0 0 0 ? * MON-FRI")
  public void generateAllClientsReport() {
    generateReport(
        forestClientRepository
            //As we are streaming through the data with Flux, we will not have the entire
            //dataset in memory at any given time
            .findAll()
            //for each entry, we will grab the data from orgbook
            .flatMap(this::mapToPublicView)
    )
        .blockOptional()
        .ifPresent(reportFile -> log.info("Generated report {} now", reportFile));
  }

  @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
  @SuppressWarnings({"java:S3864", "java:S4042"})
  public void cleanOldReports() {

    try (Stream<Path> paths = Files.list(getReportFolder())) {
      paths
          .peek(file -> log.info("Found report on folder with name {}", file.getFileName()))
          .filter(file -> isFileOlderThan(file.toFile(), 48))
          .peek(file -> log.info("Report {} is older than 48 hours, removing it", file.getFileName()))
          .forEach(path -> path.toFile().delete());
    } catch (IOException exception) {
      log.error("Error while cleaning temp folder", exception);
    }
  }

  private Path getReportFolder() {
    Path sheetFolder = Paths
        .get("./temp/")
        .normalize();

    if (!sheetFolder.toFile().exists()) {
      log.info(
          "Temporary folder for reports {} does not exist, creating {}",
          sheetFolder.toFile().getAbsolutePath(),
          sheetFolder.toFile().mkdirs()
      );
    }
    return sheetFolder;
  }

  private Path getReportPath(String reportId) {
    return getReportFolder().resolve(reportId + ".xlsx");
  }

  @SuppressWarnings("java:S4042")
  private void deleteReportFile(File file){
    if(file.exists())
      log.info("File {} deleted {}",file,file.delete());
  }

  private boolean isFileOlderThan(File file, int hoursDiff) {
    return
        Duration
            .between(
                Instant.ofEpochMilli(file.lastModified()),
                Instant.now()
            )
            .toHours() > hoursDiff;
  }

  private Mono<File> generateReport(Flux<ClientPublicViewDto> values) {

    File sheetFile = getReportPath(UUID.randomUUID().toString())
        .normalize()
        .toFile();

    SheetWriter writer = new SheetWriter(sheetFile);

    log.debug("Generating sheet file {}", sheetFile);
    return values
        //Persist it into the spreadsheet for each entry
        .doOnNext(writer::write)
        //Once all is done move ahead to complete and close the file
        .doOnComplete(writer::complete)
        //return the file
        .then(Mono.just(sheetFile));
  }

  private Mono<ClientPublicViewDto> mapToPublicView(ForestClientEntity entity) {
    return loadValueFromOrgbook(
        entity.getClientName(),
        entity.getClientNumber(),
        String.format("%s%s",
            StringUtils.defaultString(entity.getRegistryCompanyTypeCode()),
            StringUtils.defaultString(entity.getCorpRegnNmbr())
        )
    );
  }

  private Mono<ClientPublicViewDto> loadValueFromOrgbook(
      String clientName,
      String clientNumber,
      String incorporationNumber
  ) {
    return
        findOnTopic(clientName)
            .next()
            //if we find data, we build the data with it
            .map(orgBook ->
                new ClientPublicViewDto(
                    clientNumber,
                    clientName,
                    incorporationNumber,
                    orgBook.sourceId(),
                    orgBook.name(),
                    !orgBook.inactive()
                )
            )
            //if not, we use just the entity data
            .switchIfEmpty(Mono.just(
                    new ClientPublicViewDto(
                        clientNumber,
                        clientName,
                        incorporationNumber,
                        null,
                        null,
                        false
                    )
                )
            );
  }

  private Flux<OrgBookTopicDto> findOnTopic(String value) {
    return
        orgBookApi
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/search/topic")
                .queryParam("format", "json")
                .queryParam("inactive", "any")
                .queryParam("ordering", "-score")
                .queryParam("q", encodeUri(value))
                .build(Map.of())
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono(
                clientResponse -> clientResponse.bodyToMono(OrgBookTopicListResponse.class)
            )
            .onErrorReturn(
                new OrgBookTopicListResponse(0, 0, 0, new ArrayList<>())
            )
            .filter(response -> !CollectionUtils.isEmpty(response.results()))
            .flatMapIterable(OrgBookTopicListResponse::results)
            .doOnNext(
                content -> log.info("OrgBook Topic Lookup {} -> {}", value,
                    content)
            );

  }

  private static String encodeUri(String clientName) {
    return URLEncoder.encode(clientName, StandardCharsets.UTF_8);
  }

}
