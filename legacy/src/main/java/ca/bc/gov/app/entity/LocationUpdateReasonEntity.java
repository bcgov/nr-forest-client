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
@Table(name = "cli_locn_audit", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class LocationUpdateReasonEntity {

  @Id
  @Column("CLIENT_LOCATION_AUDIT_ID")
  private Long id;

  @Column("CLIENT_AUDIT_CODE")
  private String auditCode;

  @Column("CLIENT_NUMBER")
  @Size(min = 1, max = 8)
  private String clientNumber;

  @Column("CLIENT_LOCN_CODE")
  @NotNull
  @Size(min = 1, max = 2)
  private String clientLocnCode;

  @Column("CLIENT_LOCN_NAME")
  @Size(min = 1, max = 40)
  private String clientLocnName;

  @Column("HDBS_COMPANY_CODE")
  @Size(min = 1, max = 5)
  private String hdbsCompanyCode;

  @Column("ADDRESS_1")
  @NotNull
  @Size(min = 1, max = 40)
  private String addressOne;

  @Column("ADDRESS_2")
  @Size(min = 1, max = 40)
  private String addressTwo;

  @Column("ADDRESS_3")
  @Size(min = 1, max = 40)
  private String addressThree;

  @Column("CITY")
  @NotNull
  @Size(min = 1, max = 30)
  private String city;

  @Column("PROVINCE")
  @Size(min = 1, max = 50)
  private String province;

  @Column("POSTAL_CODE")
  @Size(min = 1, max = 10)
  private String postalCode;

  @Column("COUNTRY")
  @NotNull
  @Size(min = 1, max = 50)
  private String country;

  @Column("BUSINESS_PHONE")
  @Size(min = 1, max = 10)
  private String businessPhone;

  @Column("HOME_PHONE")
  @Size(min = 1, max = 10)
  private String homePhone;

  @Column("CELL_PHONE")
  @Size(min = 1, max = 10)
  private String cellPhone;

  @Column("FAX_NUMBER")
  @Size(min = 1, max = 10)
  private String faxNumber;

  @Column("EMAIL_ADDRESS")
  @Size(min = 1, max = 128)
  private String emailAddress;

  @Column("LOCN_EXPIRED_IND")
  @NotNull
  @Size(min = 1, max = 1)
  private String locnExpiredInd;

  @Column("RETURNED_MAIL_DATE")
  private LocalDateTime returnedMailDate;

  @Column("TRUST_LOCATION_IND")
  @NotNull
  @Size(min = 1, max = 1)
  private String trustLocationInd;

  @Column("CLI_LOCN_COMMENT")
  @Size(min = 1, max = 4000)
  private String cliLocnComment;

  @Column("CLIENT_UPDATE_ACTION_CODE")
  @Size(min = 1, max = 4)
  private String updateActionCode;

  @Column("CLIENT_UPDATE_REASON_CODE")
  @Size(min = 1, max = 4)
  private String updateReasonCode;

  @Column("ADD_TIMESTAMP")
  @NotNull
  private LocalDateTime createdAt;

  @Column("ADD_USERID")
  @NotNull
  @Size(min = 1, max = 30)
  private String createdBy;

  @Column("UPDATE_TIMESTAMP")
  @NotNull
  private LocalDateTime updatedAt;

  @Column("UPDATE_USERID")
  @NotNull
  @Size(min = 1, max = 30)
  private String updatedBy;

  @Column("UPDATE_ORG_UNIT")
  @NotNull
  private Long updatedByUnit;

  @Column("ADD_ORG_UNIT")
  @NotNull
  private Long createdByUnit;

  @Column("REVISION_COUNT")
  @NotNull
  private Long revision;
  
}
