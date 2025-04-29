package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "client_update_reason", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ClientUpdateReasonEntity {

  @Id
  @Column("CLIENT_UPDATE_REASON_ID")
  private Long id;

  @NotNull
  @Size(min = 1, max = 4)
  @Column("CLIENT_UPDATE_ACTION_CODE")
  private String updateActionCode;

  @NotNull
  @Size(min = 1, max = 4)
  @Column("CLIENT_UPDATE_REASON_CODE")
  private String updateReasonCode;

  @NotNull
  @Size(min = 1, max = 1)
  @Column("CLIENT_TYPE_CODE")
  private String clientTypeCode;

  @NotNull
  @Column("FOREST_CLIENT_AUDIT_ID")
  private Long forestClientAuditId;

  @Column("add_timestamp")
  private LocalDateTime createdAt;

  @Column("add_userid")
  @NotNull
  @Size(min = 1, max = 30)
  private String createdBy;

  @Column("update_timestamp")
  @NotNull
  private LocalDateTime updatedAt;

  @Column("update_userid")
  @NotNull
  @Size(min = 1, max = 30)
  private String updatedBy;

}
