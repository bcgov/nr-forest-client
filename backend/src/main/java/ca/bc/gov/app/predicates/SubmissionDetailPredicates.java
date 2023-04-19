package ca.bc.gov.app.predicates;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.query.Criteria;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionDetailPredicates implements QueryPredicates {

  public static Criteria orName(String[] values) {
    if (values != null) {
      QueryPredicates.orContains(values, "organizationName")
          .or(QueryPredicates.orContains(values, "firstName"))
          .or(QueryPredicates.orContains(values, "middleName"))
          .or(QueryPredicates.orContains(values, "lastName"));
    }
    return Criteria.empty();
  }

}
