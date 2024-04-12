package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "client_doing_business_as", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ClientDoingBusinessAsEntity {

  @Id
  @Column("client_dba_id")
  private Integer id;
  
  @Column("client_number")
  @NotNull
  @Size(min = 1, max = 8)
  private String clientNumber;
  
  @Column("doing_business_as_name")
  @NotNull
  @Size(min = 1, max = 120)
  private String doingBusinessAsName;
  
  @Column("add_timestamp")
  @NotNull
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
  
  @Column("update_org_unit")
  @NotNull
  @Size(min = 1, max = 30)
  private Long updatedByUnit;
  
  @Column("add_org_unit")
  @NotNull
  private Long createdByUnit;
  
  @Column("revision_count")
  @NotNull
  private Long revision;

}
