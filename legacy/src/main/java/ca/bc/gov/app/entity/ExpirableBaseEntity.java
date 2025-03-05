package ca.bc.gov.app.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

@SuperBuilder
@Data
@EqualsAndHashCode
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class ExpirableBaseEntity {

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

