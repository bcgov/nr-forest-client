package ca.bc.gov.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
  private String fieldId;
  private String errorMsg;
}
