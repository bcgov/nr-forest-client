package ca.bc.gov.app.entity.client;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

  @NotNull
  protected String description;

  @NotNull
  @Column("effective_date")
  protected LocalDate effectiveAt;

  @NotNull
  @Column("expiry_date")
  protected LocalDate expiredAt;

  @Column("create_timestamp")
  protected LocalDateTime createdAt;

  @Column("update_timestamp")
  protected LocalDateTime updatedAt;

  @Column("create_user")
  private String createdBy;

  @Column("update_user")
  protected String updatedBy;
}
