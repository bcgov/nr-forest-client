package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a validation error for a specific field in a data structure. This class is used to
 * capture validation failures, providing information about the field that failed validation and the
 * corresponding error message.
 */
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
@ToString
public class ValidationError implements Serializable {

  private final String fieldId;
  private final String errorMsg;
  private final String match;

  public ValidationError(){
    this.fieldId = null;
    this.errorMsg = null;
    this.match = null;
  }

  public ValidationError(String fieldId, String errorMsg) {
    this.fieldId = fieldId;
    this.errorMsg = errorMsg;
    this.match = null;
  }

  @JsonIgnore
  public boolean isValid() {
    return !StringUtils.isAllBlank(fieldId, errorMsg);
  }
}
