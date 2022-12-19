package ca.bc.gov.app.m.oracle.legacyclient.entity;

import ca.bc.gov.app.ApplicationConstant;
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
@Table(name = "CLIENT_LOCATION", schema = ApplicationConstant.ORACLE_ATTRIBUTE_SCHEMA)
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class ClientLocationEntity {

  @Id
  @Column(name = "CLIENT_NUMBER")
  private String clientNumber;

  @Column(name = "CLIENT_LOCN_CODE")
  private String clientLocnCode;

  @Column(name = "CLIENT_LOCN_NAME")
  private String clientLocnName;

  @Column(name = "HDBS_COMPANY_CODE")
  private String hdbsCompanyCode;

  @Column(name = "ADDRESS_1")
  private String addressOne;

  @Column(name = "ADDRESS_2")
  private String addressTwo;

  @Column(name = "ADDRESS_3")
  private String addressThree;

  @Column(name = "CITY")
  private String city;

  @Column(name = "PROVINCE")
  private String province;

  @Column(name = "POSTAL_CODE")
  private String postalCode;

  @Column(name = "COUNTRY")
  private String country;

  @Column(name = "BUSINESS_PHONE")
  private String businessPhone;

  @Column(name = "HOME_PHONE")
  private String homePhone;

  @Column(name = "CELL_PHONE")
  private String cellPhone;

  @Column(name = "FAX_NUMBER")
  private String faxNumber;

  @Column(name = "EMAIL_ADDRESS")
  private String emailAddress;

  @Column(name = "LOCN_EXPIRED_IND ")
  private String locnExpiredInd;

  @Column(name = "RETURNED_MAIL_DATE")
  private Date returnedMailDate;

  @Column(name = "TRUST_LOCATION_IND")
  private String trustLocationInd;

  @Column(name = "CLI_LOCN_COMMENT ")
  private String cliLocnComment;

  @Column(name = "UPDATE_TIMESTAMP")
  private Date updateTimestamp;

  @Column(name = "UPDATE_USERID")
  private String updateUserId;

  @Column(name = "UPDATE_ORG_UNIT")
  private Long updateOrgUnit;

  @Column(name = "ADD_TIMESTAMP")
  private Date addTimestamp;

  @Column(name = "ADD_USERID")
  private String addUserId;

  @Column(name = "ADD_ORG_UNIT")
  private Long addOrgUnit;

  @Column(name = "REVISION_COUNT")
  private Long revisionCount;

}
