package ca.bc.gov.app.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError implements Serializable {
  private String fieldId;
  private String errorMsg;
}
