package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.query.Criteria;

/**
 * Utility predicates and criteria helpers for relational database queries.
 */
public interface QueryPredicates {

  /**
   * Creates an OR equality criteria for a list of string values on a specific field.
   *
   * @param values the array of values to match
   * @param fieldName the column/property name
   * @return criteria matching any of the given values, or empty criteria if none
   */
  static Criteria orEqualTo(String[] values, String fieldName) {
    if (values != null) {
      return
          Stream
              .of(values)
              .filter(StringUtils::isNotBlank)
              .map(value -> where(fieldName).is(value))
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  /**
   * Creates an OR contains (LIKE) criteria for a list of string values on a specific field.
   *
   * @param values the array of values to match
   * @param fieldName the column/property name
   * @return criteria matching any of the given values, or empty criteria if none
   */
  static Criteria orContains(String[] values, String fieldName) {
    if (values != null) {
      return
          Stream
              .of(values)
              .filter(StringUtils::isNotBlank)
              .map(value -> where(fieldName).like(value).ignoreCase(true))
              .reduce(Criteria::or)
              .orElse(Criteria.empty());
    }
    return Criteria.empty();
  }

  /**
   * Creates a greater-than-or-equal criteria for a {@link LocalDateTime} value.
   *
   * @param endDate the date to compare against
   * @param fieldName the column/property name
   * @return criteria comparing greater-than-or-equal, or empty criteria if endDate is null
   */
  static Criteria isAfter(LocalDateTime endDate, String fieldName) {
    if (endDate != null) {
      return where(fieldName).greaterThanOrEquals(endDate);
    }
    return Criteria.empty();
  }

  /**
   * Creates a greater-than-or-equal criteria for a {@link LocalDate} value.
   *
   * @param endDate the date to compare against
   * @param fieldName the column/property name
   * @return criteria comparing greater-than-or-equal, or empty criteria if endDate is null
   */
  static Criteria isAfter(LocalDate endDate, String fieldName) {
    if (endDate != null) {
      return where(fieldName).greaterThanOrEquals(endDate);
    }
    return Criteria.empty();
  }

  /**
   * Creates a less-than-or-equal criteria for a {@link LocalDateTime} value.
   *
   * @param startDate the date to compare against
   * @param fieldName the column/property name
   * @return criteria comparing less-than-or-equal, or empty criteria if startDate is null
   */
  static Criteria isBefore(LocalDateTime startDate, String fieldName) {
    if (startDate != null) {
      return where(fieldName).lessThanOrEquals(startDate);
    }
    return Criteria.empty();
  }

  /**
   * Creates a less-than-or-equal criteria for a {@link LocalDate} value.
   *
   * @param startDate the date to compare against
   * @param fieldName the column/property name
   * @return criteria comparing less-than-or-equal, or empty criteria if startDate is null
   */
  static Criteria isBefore(LocalDate startDate, String fieldName) {
    if (startDate != null) {
      return where(fieldName).lessThanOrEquals(startDate);
    }
    return Criteria.empty();
  }

  /**
   * Creates an IS NULL criteria for a given field name.
   *
   * @param fieldName the column/property name
   * @return criteria checking if the column is null
   */
  static Criteria isNull(String fieldName) {
    return where(fieldName).isNull();
  }

}
