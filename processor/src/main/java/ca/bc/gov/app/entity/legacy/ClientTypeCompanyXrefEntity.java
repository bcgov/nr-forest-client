package ca.bc.gov.app.entity.legacy;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "CLIENT_TYPE_COMPANY_XREF", schema = "THE")
public class ClientTypeCompanyXrefEntity {

  @Column("CLIENT_TYPE_CODE")
  private String clientTypeCode;
  @Column("REGISTRY_COMPANY_TYPE_CODE")
  private String registryCompanyTypeCode;
  @Column("ADD_TIMESTAMP")
  private LocalDateTime createdAt;
  @Column("ADD_USERID")
  private String createdBy;
  @Column("UPDATE_TIMESTAMP")
  private LocalDateTime updatedAt;
  @Column("UPDATE_USERID")
  private String updatedBy;
}
