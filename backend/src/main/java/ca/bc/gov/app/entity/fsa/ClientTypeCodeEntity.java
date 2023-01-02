package ca.bc.gov.app.entity.fsa;

import ca.bc.gov.app.ApplicationConstant;
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


@Table(name = "client_type_code", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class ClientTypeCodeEntity {

  @Id
  @Column("client_type_code")
  private String code;

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
