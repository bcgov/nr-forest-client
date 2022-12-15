package ca.bc.gov.app.m.oracle.legacyclient.entity;

import ca.bc.gov.app.core.CoreConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "FOREST_CLIENT", schema = CoreConstant.ORACLE_ATTRIBUTE_SCHEMA)
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class ForestClientEntity {

  @Id
  @Column(name = "CLIENT_NUMBER")
  private String clientNumber;

  @Column(name = "CLIENT_NAME")
  private String clientName;

  @Column(name = "LEGAL_FIRST_NAME")
  private String legalFirstName;

  @Column(name = "LEGAL_MIDDLE_NAME")
  private String legalMiddleName;

  @Column(name = "CLIENT_STATUS_CODE")
  private String clientStatusCode;

  @Column(name = "CLIENT_TYPE_CODE")
  private String clientTypeCode;

  @Column(name = "BIRTHDATE")
  private Date birthdate;

  @Column(name = "CLIENT_ID_TYPE_CODE")
  private String clientIdTypeCode;

  @Column(name = "CLIENT_IDENTIFICATION ")
  private String clientIdentification;

  @Column(name = "REGISTRY_COMPANY_TYPE_CODE")
  private String registryCompanyTypeCode;

  @Column(name = "CORP_REGN_NMBR")
  private String corpRegnNmbr;

  @Column(name = "CLIENT_ACRONYM")
  private String clientAcronym;

  @Column(name = "WCB_FIRM_NUMBER")
  private String wcbFirmNumber;

  @Column(name = "OCG_SUPPLIER_NMBR")
  private String ocgSupplierNmbr;

  @Column(name = "CLIENT_COMMENT")
  private String clientComment;

  @Column(name = "ADD_TIMESTAMP")
  private Date addTimestamp;

  @Column(name = "ADD_USERID")
  private String addUserId;

  @Column(name = "ADD_ORG_UNIT")
  private Long addOrgUnit;

  @Column(name = "UPDATE_TIMESTAMP")
  private Date updateTimestamp;

  @Column(name = "UPDATE_USERID")
  private String updateUserId;

  @Column(name = "UPDATE_ORG_UNIT")
  private Long updateOrgUnit;

  @Column(name = "REVISION_COUNT")
  private Long revisionCount;

}
