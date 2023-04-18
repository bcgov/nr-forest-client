package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.springframework.data.relational.core.query.Criteria;

public class SubmissionPredicates extends AbstractQueryPredicates {

  public static Criteria orStatus(String[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .map(value ->
                  where("submissionStatus")
                      .is(SubmissionStatusEnum.fromValue(value))
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
