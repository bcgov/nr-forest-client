package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.query.Criteria;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionPredicates implements QueryPredicates {

  public static Criteria orStatus(SubmissionStatusEnum[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .map(value ->
                  where("submissionStatus")
                      .is(value)
              )
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  public static Criteria orUpdatedAt(String[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .map(value ->
                  where("updatedAt")
                      .lessThanOrEquals(
                          LocalDate
                              .parse(
                                  value,
                                  DateTimeFormatter.ISO_DATE
                              )
                              .plusDays(1)
                      )
              )
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

}
