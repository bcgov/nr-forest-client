package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.ForestClientInformationDto;
import ca.bc.gov.app.dto.HistoryLogDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.entity.ClientRelatedProjection;
import ca.bc.gov.app.entity.ForestClientEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ForestClientRepository extends ReactiveCrudRepository<ForestClientEntity, String>,
    ReactiveQueryByExampleExecutor<ForestClientEntity>,
    ReactiveSortingRepository<ForestClientEntity, String> {

  Flux<ForestClientEntity> findBy(Pageable page);

  @Query(ForestClientQueries.FIND_CLIENT_BY_REGISTRATION_OR_NAME)
  Flux<ForestClientEntity> findClientByIncorporationOrName(
      @Param("registrationNumber") String registrationNumber,
      @Param("companyName") String companyName
  );

  @Query(ForestClientQueries.FIND_FUZZY_INDIVIDUAL_BY_NAME_AND_DOB)
  Flux<ForestClientEntity> findByIndividualFuzzy(String name, LocalDateTime dob);

  @Query(ForestClientQueries.FIND_FUZZY_CLIENT_BY_NAME)
  Flux<ForestClientEntity> matchBy(String companyName);

  Mono<ForestClientEntity> findByClientNumber(String clientNumber);
  
  @Query(ForestClientQueries.FIND_CLIENT_DETAILS_BY_CLIENT_NUMBER)
  Mono<ForestClientInformationDto> findDetailsByClientNumber(String clientNumber);

  @Query(ForestClientQueries.FIND_BY_PREDICTIVE_SEARCH_WITH_LIKE)
  Flux<PredictiveSearchResultDto> findByPredictiveSearchWithLike(
      String value, int limit, long offset);

  @Query(ForestClientQueries.COUNT_BY_PREDICTIVE_SEARCH_WITH_LIKE)
  Mono<Long> countByPredictiveSearchWithLike(String value);
  
  @Query(ForestClientQueries.FIND_BY_PREDICTIVE_SEARCH_WITH_SIMILARITY)
  Flux<PredictiveSearchResultDto> findByPredictiveSearchWithSimilarity(
      String value, int limit, long offset);

  @Query(ForestClientQueries.COUNT_BY_PREDICTIVE_SEARCH_WITH_SIMILARITY)
  Mono<Long> countByPredictiveSearchWithSimilarity(String value);

  @Query(ForestClientQueries.FIND_BY_EMPTY_FULL_SEARCH)
  Flux<PredictiveSearchResultDto> findByEmptyFullSearch(
      int limit, long offset, LocalDateTime date);
  
  @Query(ForestClientQueries.COUNT_BY_EMPTY_FULL_SEARCH)
  Mono<Long> countByEmptyFullSearch(LocalDateTime date);
  
  @Query(ForestClientQueries.CLIENT_INFORMATION_HISTORY)
  Flux<HistoryLogDto> findClientInformationHistoryLogsByClientNumber(String clientNumber);
  
  @Query(ForestClientQueries.LOCATION_HISTORY)
  Flux<HistoryLogDto> findLocationHistoryLogsByClientNumber(String clientNumber);
  
  @Query(ForestClientQueries.CONTACT_HISTORY)
  Flux<HistoryLogDto> findContactHistoryLogsByClientNumber(String clientNumber);
  
  @Query(ForestClientQueries.DOING_BUSINESS_AS_HISTORY)
  Flux<HistoryLogDto> findDoingBusinessAsHistoryLogsByClientNumber(String clientNumber);
  
  @Query(ForestClientQueries.RELATED_CLIENT_HISTORY)
  Flux<HistoryLogDto> findRelatedClientAsHistoryLogsByClientNumber(String clientNumber);

  @Query(ForestClientQueries.SEARCH_OTHER_CORP_NUMBER)
  Flux<ForestClientEntity> findByCompanyTypeOrNumber(
      String clientNumber,
      String companyType,
      String companyNumber
  );

  @Query(ForestClientQueries.RELATED_CLIENT_LIST)
  Flux<ClientRelatedProjection> findByClientRelatedList(String clientNumber);

  @Query(ForestClientQueries.RELATED_CLIENT_AUTOCOMPLETE_WITH_LIKE)
  Flux<PredictiveSearchResultDto> findByRelatedClientWithLike(
      String mainClientNumber, String relationType, String value, int limit, long offset);

  @Query(ForestClientQueries.RELATED_CLIENT_AUTOCOMPLETE_COUNT_WITH_LIKE)
  Mono<Long> countByRelatedClientWithLike(String mainClientNumber, String relationType, String value);

  @Query(ForestClientQueries.RELATED_CLIENT_AUTOCOMPLETE_WITH_SIMILARITY)
  Flux<PredictiveSearchResultDto> findByRelatedClientWithSimilarity(
      String mainClientNumber, String relationType, String value, int limit, long offset);

  @Query(ForestClientQueries.RELATED_CLIENT_AUTOCOMPLETE_COUNT_WITH_SIMILARITY)
  Mono<Long> countByRelatedClientWithSimilarity(String mainClientNumber, String relationType, String value);
  
}