package ca.bc.gov.app.predicates;

import static org.springframework.data.relational.core.query.Criteria.where;

import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.query.Criteria;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractQueryPredicates {

  public static Criteria orEqualTo(String[] values, String fieldName) {
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

  public static Criteria orContains(String[] values, String fieldName) {
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

}
