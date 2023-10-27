package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.query.Criteria;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionPredicates implements QueryPredicates {

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

  public static Criteria orUpdatedAt(String[] values) {
    if (values != null) {
      return
          Stream
              .of(values)
              .filter(StringUtils::isNotBlank)
              .map(value -> LocalDate.parse(value,DateTimeFormatter.ISO_DATE))
              .map(value -> value.plusDays(1))
              .map(value -> QueryPredicates.isBefore(value.atStartOfDay(),"updatedAt"))
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

}
