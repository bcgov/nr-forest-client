package ca.bc.gov.app.entity.client;


import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "contact_type_code", schema = "nrfc")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class ContactTypeCodeEntity {
  @Id
  @Column("contact_type_code")
  private String contactTypeCode;

  @NotNull
  @Column("description")
  private String description;

  @NotNull
  @Column("effective_date")
  private LocalDate effectiveAt;

  @NotNull
  @Column("expiry_date")
  private LocalDate expiredAt;

  @Column("create_timestamp")
  private LocalDateTime createdAt;

  @Column("update_timestamp")
  private LocalDateTime updatedAt;

  @Column("create_user")
  private String createdBy;

  @Column("update_user")
  private String updatedBy;
}
