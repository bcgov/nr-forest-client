package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "client_status_code", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ClientStatusCodeEntity {

  @Id
  @Column("client_status_code")
  @NotNull
  @Size(min = 1, max = 3)
  private String clientStatusCode;

  @Column("description")
  @NotNull
  @Size(min = 1, max = 120)
  private String description;

  @Column("effective_date")
  @NotNull
  private LocalDate effectiveDate;

  @Column("expiry_date")
  @NotNull
  private LocalDate expiryDate;

}
