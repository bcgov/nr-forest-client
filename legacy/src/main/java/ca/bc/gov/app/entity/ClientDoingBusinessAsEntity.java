package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

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
@Table(name = "CLIENT_DOING_BUSINESS_AS", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ClientDoingBusinessAsEntity {

  @Id
  @Column("CLIENT_DBA_ID")
  private Integer id;
  @Column("CLIENT_NUMBER")
  private String clientNumber;
  @Column("DOING_BUSINESS_AS_NAME")
  private String doingBusinessAsName;
  @Column("ADD_TIMESTAMP")
  private LocalDateTime createdAt;
  @Column("ADD_USERID")
  private String createdBy;
  @Column("UPDATE_TIMESTAMP")
  private LocalDateTime updatedAt;
  @Column("UPDATE_USERID")
  private String updatedBy;
  @Column("UPDATE_ORG_UNIT")
  private Long updatedByUnit;
  @Column("ADD_ORG_UNIT")
  private Long createdByUnit;
  @Column("REVISION_COUNT")
  private Long revision;

}
