package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.springframework.data.relational.core.query.Criteria;

public interface QueryPredicates {

  static Criteria orEqualTo(String[] values, String fieldName) {
    if (values != null) {
      return
          Stream
              .of(values)
              .map(value -> where(fieldName).is(value))
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  static Criteria orContains(String[] values, String fieldName) {
    if (values != null) {
      return
          Stream
              .of(values)
              .map(value -> where(fieldName).like(value).ignoreCase(true))
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  static Criteria isDateBetween(LocalDateTime startDate, LocalDateTime endDate, String fieldName) {
    if (startDate != null && endDate != null) {
      return where(fieldName).between(startDate, endDate);
    }
    return Criteria.empty();
  }

  static Criteria isBefore(LocalDateTime endDate, String fieldName) {
    if (endDate != null) {
      return where(fieldName).greaterThanOrEquals(endDate);
    }
    return Criteria.empty();
  }

  static Criteria isAfter(LocalDateTime startDate, String fieldName) {
    if (startDate != null) {
      return where(fieldName).lessThanOrEquals(startDate);
    }
    return Criteria.empty();
  }

}
