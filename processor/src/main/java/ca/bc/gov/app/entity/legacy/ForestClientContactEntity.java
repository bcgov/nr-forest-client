package ca.bc.gov.app.entity.legacy;

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
@Table(name = "CLIENT_CONTACT", schema = "THE")
public class ForestClientContactEntity {

  @Id
  @Column("CLIENT_CONTACT_ID")
  private String clientContactId;
  @Column("CLIENT_NUMBER")
  private String clientNumber;
  @Column("CLIENT_LOCN_CODE")
  private String clientLocnCode;
  @Column("BUS_CONTACT_CODE")
  private String contactCode;
  @Column("CONTACT_NAME")
  private String contactName;
  @Column("BUSINESS_PHONE")
  private String businessPhone;
  @Column("CELL_PHONE")
  private String cellPhone;
  @Column("FAX_NUMBER")
  private String faxNumber;
  @Column("EMAIL_ADDRESS")
  private String emailAddress;
  @Column("ADD_TIMESTAMP")
  private LocalDateTime createdAt;
  @Column("ADD_USERID")
  private String createdBy;
  @Column("UPDATE_TIMESTAMP")
  private LocalDateTime updatedAt;
  @Column("UPDATE_USERID")
  private String updatedBy;
  @Column("UPDATE_ORG_UNIT")
  private Long updateOrgUnit;
  @Column("ADD_ORG_UNIT")
  private Long addOrgUnit;
  @Column("REVISION_COUNT")
  private Long revision;
}
