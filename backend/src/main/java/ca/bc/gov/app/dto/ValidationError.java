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

  private static final long serialVersionUID = -3525306340782994707L;
  
  private String fieldId;
  private String errorMsg;
  
}
