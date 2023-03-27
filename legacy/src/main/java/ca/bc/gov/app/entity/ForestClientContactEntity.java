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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "CLIENT_CONTACT", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientContactEntity {

  @Id
  @Column("CLIENT_CONTACT_ID")
  private Long contactId;
  @Column("CLIENT_NUMBER")
  private String clientNumber;
  @Column("CLIENT_LOCN_CODE")
  private String clientLocnCode;
  @Column("BUS_CONTACT_CODE")
  private String contactCode;
  @Column("CONTACT_NAME")
  private String name;
  @Column("BUSINESS_PHONE")
  private String businessPhone;
  @Column("CELL_PHONE")
  private String cellPhone;
  @Column("FAX_NUMBER")
  private String faxNumber;
  @Column("EMAIL_ADDRESS")
  private String emailAddress;
  @Column("UPDATE_TIMESTAMP")
  private LocalDate updateTimestamp;
  @Column("UPDATE_USERID")
  private String updateUserId;
  @Column("UPDATE_ORG_UNIT")
  private Long updateOrgUnit;
  @Column("ADD_TIMESTAMP")
  private LocalDate addTimestamp;
  @Column("ADD_USERID")
  private String addUserId;
  @Column("ADD_ORG_UNIT")
  private Long addOrgUnit;
  @Column("REVISION_COUNT")
  private Long revisionCount;
}
