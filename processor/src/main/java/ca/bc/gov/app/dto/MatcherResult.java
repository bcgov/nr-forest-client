package ca.bc.gov.app.dto;

import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * This record represents the result of a matching operation.
 * It contains the name of the field that was matched and a set of values that were found.
 */
public record MatcherResult(
    String fieldName,  // The name of the field that was matched
    Set<String> values  // The set of values that were found
) {

  /**
   * This method is used to add a value to the set of values.
   * It checks if the value is not blank and the set of values is not null before adding the value.
   *
   * @param value The value to be added to the set of values.
   */
  public void addValue(String value) {
    if (StringUtils.isNotBlank(value) && this.values != null) {
      this.values.add(value);
    }
  }

  /**
   * This method is used to get a string representation of the set of values.
   * It joins the values with a comma.
   *
   * @return A string representation of the set of values.
   */
  public String value() {
    return String.join(",", this.values);
  }

  /**
   * This method is used to check if there is a match.
   * It checks if the set of values is not null and not empty.
   *
   * @return A boolean indicating if there is a match.
   */
  public boolean hasMatch() {
    return this.values != null && !this.values.isEmpty();
  }
}