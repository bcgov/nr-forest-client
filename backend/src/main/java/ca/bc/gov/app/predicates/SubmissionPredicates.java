package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.models.client.SubmissionTypeCodeEnum;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.query.Criteria;

/**
 * Predicates and criteria builders for submission queries.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionPredicates implements QueryPredicates {

  /**
   * Builds an OR criteria for the given submission types.
   *
   * @param values array of submission type codes to match
   * @return criteria matching any of the specified types, or empty criteria if null/empty
   */
  public static Criteria orType(SubmissionTypeCodeEnum[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .filter(Objects::nonNull)
              .map(value ->
                  where("submissionType")
                      .is(value)
              )
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  /**
   * Builds an OR criteria for the given submission statuses.
   *
   * @param values array of submission statuses to match
   * @return criteria matching any of the specified statuses, or empty criteria if null/empty
   */
  public static Criteria orStatus(SubmissionStatusEnum[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .filter(Objects::nonNull)
              .map(value ->
                  where("submissionStatus")
                      .is(value)
              )
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  /**
   * Builds an OR criteria for submissions on or before the given dates.
   *
   * @param values array of date strings to match
   * @return criteria matching submissions on or before the given dates, or empty criteria
   */
  public static Criteria orSubmittedAt(String[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .filter(StringUtils::isNotBlank)
              .map(value -> LocalDate.parse(value, DateTimeFormatter.ISO_DATE))
              .map(value -> value.plusDays(1))
              .map(value -> QueryPredicates.isBefore(value.atStartOfDay(), "submittedAt"))
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

}
