package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "client_update_reason_code", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ClientUpdateReasonCodeEntity {

  @Id
  @Column("client_update_reason_code")
  @NotNull
  @Size(min = 1, max = 4)
  private String code;
  
  @NotNull
  @Size(min = 5, max = 120)
  protected String description;
  
}
