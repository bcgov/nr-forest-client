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
@Table(name = "FOREST_CLIENT", schema = "THE")
public class ForestClientEntity {

  @Id
  @Column("CLIENT_NUMBER")
  private String clientNumber;
  @Column("CLIENT_NAME")
  private String clientName;
  @Column("LEGAL_FIRST_NAME")
  private String legalFirstName;
  @Column("LEGAL_MIDDLE_NAME")
  private String legalMiddleName;
  @Column("CLIENT_STATUS_CODE")
  private String clientStatusCode;
  @Column("CLIENT_TYPE_CODE")
  private String clientTypeCode;
  @Column("CLIENT_ID_TYPE_CODE")
  private String clientIdTypeCode;
  @Column("CLIENT_IDENTIFICATION ")
  private String clientIdentification;
  @Column("REGISTRY_COMPANY_TYPE_CODE")
  private String registryCompanyTypeCode;
  @Column("CORP_REGN_NMBR")
  private String corpRegnNmbr;
  @Column("CLIENT_ACRONYM")
  private String clientAcronym;
  @Column("WCB_FIRM_NUMBER")
  private String wcbFirmNumber;
  @Column("OCG_SUPPLIER_NMBR")
  private String ocgSupplierNmbr;
  @Column("CLIENT_COMMENT")
  private String clientComment;
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
