package ca.bc.gov.app.predicates;

import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.query.Criteria;

@NoArgsConstructor
public class SubmissionDetailPredicates extends AbstractQueryPredicates {

  public static Criteria orName(String[] values) {
    if (values != null) {
      orContains(values, "organizationName")
          .or(orContains(values, "firstName"))
          .or(orContains(values, "middleName"))
          .or(orContains(values, "lastName"));
    }
    return Criteria.empty();
  }

}
