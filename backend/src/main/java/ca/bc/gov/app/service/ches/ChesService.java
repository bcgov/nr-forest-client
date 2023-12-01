package ca.bc.gov.app.service.ches;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.ches.ChesMailBodyType;
import ca.bc.gov.app.dto.ches.ChesMailEncoding;
import ca.bc.gov.app.dto.ches.ChesMailErrorResponse;
import ca.bc.gov.app.dto.ches.ChesMailPriority;
import ca.bc.gov.app.dto.ches.ChesMailRequest;
import ca.bc.gov.app.dto.ches.ChesMailResponse;
import ca.bc.gov.app.dto.ches.ChesRequestDto;
import ca.bc.gov.app.dto.ches.CommonExposureJwtDto;
import ca.bc.gov.app.dto.client.EmailLogDto;
import ca.bc.gov.app.entity.client.EmailLogEntity;
import ca.bc.gov.app.exception.*;
import ca.bc.gov.app.repository.client.EmailLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.r2dbc.postgresql.codec.Json;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChesService {

  public static final String FAILED_TO_SEND_EMAIL = "Failed to send email: {}";
  private final ForestClientConfiguration configuration;
  private final WebClient chesApi;

  private final WebClient authApi;
  private final Configuration freeMarkerConfiguration;
  
  private final EmailLogRepository emailLogRepository;
  
  private final Jackson2ObjectMapperBuilder builder;

  public ChesService(
      ForestClientConfiguration configuration,
      @Qualifier("chesApi") WebClient chesApi,
      @Qualifier("authApi") WebClient authApi,
      EmailLogRepository emailLogRepository,
      Jackson2ObjectMapperBuilder builder
  ) {
    this.configuration = configuration;
    this.chesApi = chesApi;
    this.authApi = authApi;
    this.freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_31);
    this.emailLogRepository = emailLogRepository;
    this.builder = builder;
    freeMarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/templates");
    freeMarkerConfiguration.setDefaultEncoding("UTF-8");
  }

  public Mono<String> sendEmail(String templateName,
                                String emailAddress, 
                                String subject, 
                                Map<String, Object> variables,
                                Integer emailLogId) {
    return this.buildTemplate(templateName, variables).flatMap(body -> {
      ChesRequestDto chesRequestDto = new ChesRequestDto(List.of(emailAddress, 
                                                                 "paulo.cruz@gov.bc.ca",
                                                                 "ziad.bhunnoo@gov.bc.ca",
                                                                 "maria.martinez@gov.bc.ca"),
                                                         body);

      return this.sendEmail(chesRequestDto, subject).flatMap(emailId -> {
        log.info("Mail sent, transaction ID is {}", emailId);
        
        EmailLogDto emailLogDto = new EmailLogDto(
            emailLogId,
            templateName, 
            emailAddress, 
            subject, 
            "Y", 
            emailId,
            "", 
            variables);
        return saveEmailLog(emailLogDto, "Email sent successfully. Transaction ID: " + emailId);
      })
      .onErrorResume(throwable -> {
        log.error("Error occurred while building/sending the email: {}", throwable.getMessage());

        EmailLogDto emailLogDto = new EmailLogDto(
            emailLogId,
            templateName,
            emailAddress,
            subject,
            "N",
            "",
            throwable.getMessage(),
            variables);
        return saveEmailLog(emailLogDto,  "Error sending email");
      });
    });
  }
  
  private Mono<String> saveEmailLog(EmailLogDto emailLogDto, String transactionMsg) {
    if (emailLogDto.emailLogId() != null) {
        return emailLogRepository.findById(emailLogDto.emailLogId())
                .flatMap(existingLogEntity -> updateExistingLogEntity(
                                                existingLogEntity, 
                                                emailLogDto, 
                                                transactionMsg));
    } else {
        EmailLogEntity logEntity = createNewLogEntity(emailLogDto);
        return emailLogRepository.save(logEntity)
                .thenReturn(transactionMsg);
    }
  }

  private Mono<String> updateExistingLogEntity(
                        EmailLogEntity existingLogEntity, 
                        EmailLogDto emailLogDto, 
                        String transactionMsg) {
    
      String exceptionMessage = "Y".equals(existingLogEntity.getEmailSentInd()) 
                                      ? "" 
                                      : emailLogDto.exceptionMessage();
      existingLogEntity.setEmailSentInd(emailLogDto.emailSentInd());
      existingLogEntity.setExceptionMessage(exceptionMessage);
      existingLogEntity.setUpdateDate(LocalDateTime.now());
  
      return emailLogRepository
              .save(existingLogEntity)
              .thenReturn(transactionMsg);
  }
  
  private EmailLogEntity createNewLogEntity(EmailLogDto emailLogDto) {
      EmailLogEntity logEntity = new EmailLogEntity();
      logEntity.setCreateDate(LocalDateTime.now());
      logEntity.setTemplateName(emailLogDto.templateName());
      logEntity.setEmailAddress(emailLogDto.emailAddress());
      logEntity.setEmailSubject(emailLogDto.subject());
      logEntity.setEmailSentInd(emailLogDto.emailSentInd());
      logEntity.setEmailId(emailLogDto.emailId());
      logEntity.setExceptionMessage(emailLogDto.exceptionMessage());
      logEntity.setEmailVariables(convertTo(emailLogDto.variables()));
  
      return logEntity;
  }

  private Json convertTo(Map<String, Object> variables) {
    String json = "{}";

    try {
      json = builder
              .build()
              .writeValueAsString(variables);
    } catch (JsonProcessingException e) {
      log.error("Error while converting matchers to json", e);
    }

    return Json.of(json);
  }

  /**
   * Sends an email using the BC Government's Common Email Service (Ches) via HTTP POST request
   * using WebClient.
   *
   * @param requestContent the {@link ChesRequestDto} object representing the email to be sent
   * @return a {@link Mono} that the transaction ID of the email send operation upon completion
   * @throws InvalidAccessTokenException if the authorization token is invalid or expired
   * @throws InvalidRoleException        if does not have the required role to perform the requested
   *                                     action
   */
  public Mono<String> sendEmail(ChesRequestDto requestContent, String subject) {

    if (requestContent == null) {
      return Mono.error(new InvalidRequestObjectException("no request body was provided"));
    }

    return
        Mono
            .just(requestContent)
            .map(request ->
                new ChesMailRequest(
                    null,
                    null,
                    ChesMailBodyType.HTML,
                    request.emailBody(),
                    null,
                    0,
                    ChesMailEncoding.UTF_8,
                    "FSA_donotreply@gov.bc.ca",
                    ChesMailPriority.NORMAL,
                    subject,
                    null,
                    request.emailTo()
                )
            )
            .flatMap(request ->
                getToken()
                    .flatMap(token ->
                        chesApi
                            .post()
                            .uri(configuration.getChes().getUri())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(Mono.just(request), ChesMailRequest.class)
                            .retrieve()
                            .onStatus(httpStatusCode -> httpStatusCode.value() == 401,
                                response -> Mono.error(new InvalidAccessTokenException()))
                            .onStatus(httpStatusCode -> httpStatusCode.value() == 403,
                                response -> Mono.error(new InvalidRoleException()))

                            .onStatus(httpStatusCode -> httpStatusCode.value() == 400,
                                get400ErrorMessage())
                            .onStatus(httpStatusCode -> httpStatusCode.value() == 422,
                                get422ErrorMessage())
                            .onStatus(HttpStatusCode::isError, get500ErrorMessage())
                            .bodyToMono(ChesMailResponse.class)
                            .doOnNext(response -> log.info("Email sent successfully"))
                            .doOnError(error -> log.error("Failed to send email", error))
                    )
            )
            .map(response -> response.txId().toString())
            .doOnError(error -> log.error("Failed to send email", error));

  }

  /**
   * Builds a String representation of an HTML email template by applying a map of variables to the
   * FreeMarker template identified by the given template name.
   *
   * @param templateName the name of the FreeMarker template, without the ".html" extension
   * @param variables    a map of variable names and their corresponding values to be used when
   *                     processing the template
   * @return a Mono that emits the String representation of the processed template, or an error if
   *         an exception occurs during template processing
   */
  public Mono<String> buildTemplate(String templateName, Map<String, Object> variables) {
    StringWriter writer = new StringWriter();
    try {
      Template template = freeMarkerConfiguration.getTemplate(templateName + ".html");
      template.process(variables, writer);
    } catch (TemplateException | IOException e) {
      return Mono.error(e);
    }

    return Mono.just(writer.toString());
  }

  private static Function<ClientResponse, Mono<? extends Throwable>> get500ErrorMessage() {
    return response ->
        response
            .bodyToMono(ChesMailErrorResponse.class)
            .doOnNext(error -> log.error(FAILED_TO_SEND_EMAIL, error))
            .flatMap(errorMessageDetail -> Mono.error(
                new UnexpectedErrorException(errorMessageDetail.status(),
                    errorMessageDetail.detail())));
  }

  private static Function<ClientResponse, Mono<? extends Throwable>> get422ErrorMessage() {
    return response ->
        response
            .bodyToMono(ChesMailErrorResponse.class)
            .doOnNext(error -> log.error(FAILED_TO_SEND_EMAIL, error))
            .map(details ->
                Optional
                    .ofNullable(details.errors())
                    .map(errorList -> errorList
                        .stream()
                        .map(errorEntry -> String.format("%s on %s", errorEntry.message(),
                            errorEntry.value()))
                        .collect(Collectors.joining(","))
                    )
                    .map(errors -> String.join(",", errors, details.detail()))
                    .orElse(details.detail())
            )
            .flatMap(errorMessageDetail -> Mono.error(
                new UnableToProcessRequestException(errorMessageDetail)));
  }

  private static Function<ClientResponse, Mono<? extends Throwable>> get400ErrorMessage() {
    return response ->
        response
            .bodyToMono(ChesMailErrorResponse.class)
            .map(ChesMailErrorResponse::detail)
            .doOnNext(error -> log.error(FAILED_TO_SEND_EMAIL, error))
            .flatMap(errorMessageDetail -> Mono.error(
                new BadRequestException(errorMessageDetail)));
  }

  private Mono<String> getToken() {

    return
        authApi
            .post()
            .uri(configuration.getChes().getTokenUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
            .retrieve()
            .onStatus(httpStatusCode -> httpStatusCode.value() == 401,
                response -> Mono.error(new InvalidAccessTokenException()))
            .onStatus(httpStatusCode -> httpStatusCode.value() == 403,
                response -> Mono.error(new InvalidRoleException()))
            .onStatus(httpStatusCode -> httpStatusCode.value() == 400, get400ErrorMessage())
            .onStatus(httpStatusCode -> httpStatusCode.value() == 422, get422ErrorMessage())
            .onStatus(HttpStatusCode::isError, get500ErrorMessage())
            .bodyToMono(CommonExposureJwtDto.class)
            .map(CommonExposureJwtDto::accessToken)
            .doOnNext(token -> log.info("Successfully retrieved access token"));
  }

}
