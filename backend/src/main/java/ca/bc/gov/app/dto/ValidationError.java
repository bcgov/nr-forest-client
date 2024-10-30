package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a validation error for a specific field in a data structure.
 * This class is used to capture validation failures, providing information
 * about the field that failed validation and the corresponding error message.
 *
 * @param fieldId The identifier of the field that failed validation.
 * @param errorMsg The error message describing the validation failure.
 */
public record ValidationError(String fieldId, String errorMsg) implements Serializable {
  @JsonIgnore
  public boolean isValid() {
    return !StringUtils.isAllBlank(fieldId, errorMsg);
  }
}
