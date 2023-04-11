package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.dto.OrgBookTopicDto;
import ca.bc.gov.app.dto.OrgBookTopicListResponse;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.SheetWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
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
  private final ClientDoingBusinessAsRepository clientDoingBusinessAsRepository;
  private final WebClient orgBookApi;

  public ReportingClientService(ForestClientRepository forestClientRepository,
                                ClientDoingBusinessAsRepository clientDoingBusinessAsRepository,
                                @Qualifier("orgBookApi") WebClient orgBookApi) {
    this.forestClientRepository = forestClientRepository;
    this.clientDoingBusinessAsRepository = clientDoingBusinessAsRepository;
    this.orgBookApi = orgBookApi;
  }

  public Mono<File> generateDoingBusinessAsReport() {

    return
        generateReport(
            clientDoingBusinessAsRepository
                //As we are streaming through the data with Flux, we will not have the entire
                //dataset in memory at any given time
                .findAll()
                //for each entry, we will grab the data from orgbook
                .flatMap(this::mapToPublicView)
        );
  }

  public Mono<File> generateAllClientsReport() {
    return
        generateReport(
            forestClientRepository
                //As we are streaming through the data with Flux, we will not have the entire
                //dataset in memory at any given time
                .findAll()
                //for each entry, we will grab the data from orgbook
                .flatMap(this::mapToPublicView)
        );
  }

  @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
  @SuppressWarnings({"java:S3864", "java:S4042"})
  public void cleanOldReports() {

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

    try (Stream<Path> paths = Files.list(sheetFolder)) {
      paths
          .peek(file -> log.info("Found report on folder with name {}", file.getFileName()))
          .filter(file -> isFileOlderThan(file.toFile(), 5))
          .forEach(path -> path.toFile().delete());
    } catch (IOException exception) {
      log.error("Error while cleaning temp folder", exception);
    }


  }

  private boolean isFileOlderThan(File file, int minutesDiff) {
    return
        Duration
            .between(
                Instant.ofEpochMilli(file.lastModified()),
                Instant.now()
            )
            .toMinutes() > minutesDiff;
  }

  private Mono<File> generateReport(Flux<ClientPublicViewDto> values) {

    File sheetFile = Paths
        .get("./temp/", UUID.randomUUID().toString() + ".xlsx")
        .normalize()
        .toFile();

    if (!sheetFile.getParentFile().exists()) {
      log.info(
          "Temporary folder for reports {} does not exist, creating {}",
          sheetFile.getParentFile().getAbsolutePath(),
          sheetFile.getParentFile().mkdirs()
      );
    }

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

  private Mono<ClientPublicViewDto> mapToPublicView(ClientDoingBusinessAsEntity entity) {
    return loadValueFromOrgbook(
        entity.getDoingBusinessAsName(),
        entity.getClientNumber(),
        StringUtils.EMPTY
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
            .filter(response -> !CollectionUtils.isEmpty(response.results()))
            .flatMapIterable(OrgBookTopicListResponse::results)
            .doOnNext(
                content -> log.info("OrgBook Topic Lookup {} -> {}", value,
                    content)
            );

  }

  private static String encodeUri(String clientName) {
    try {
      return URLEncoder.encode(clientName, "utf-8");
    } catch (UnsupportedEncodingException e) {
      return clientName;
    }
  }

}
