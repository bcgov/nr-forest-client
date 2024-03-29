package ca.bc.gov.app.entity;

import java.time.LocalDate;
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
@Table(name = "CLIENT_LOCATION", schema = "THE")
public class ForestClientLocationEntity {

  @Id
  @Column("CLIENT_NUMBER")
  private String clientNumber;
  
  @Column("CLIENT_LOCN_CODE")
  private String clientLocnCode;
  
  @Column("CLIENT_LOCN_NAME")
  private String clientLocnName;
  
  @Column("HDBS_COMPANY_CODE")
  private String hdbsCompanyCode;
  
  @Column("ADDRESS_1")
  private String addressOne;
  
  @Column("ADDRESS_2")
  private String addressTwo;
  
  @Column("ADDRESS_3")
  private String addressThree;
  
  @Column("CITY")
  private String city;
  
  @Column("PROVINCE")
  private String province;
  
  @Column("POSTAL_CODE")
  private String postalCode;
  
  @Column("COUNTRY")
  private String country;
  
  @Column("BUSINESS_PHONE")
  private String businessPhone;
  
  @Column("HOME_PHONE")
  private String homePhone;
  
  @Column("CELL_PHONE")
  private String cellPhone;
  
  @Column("FAX_NUMBER")
  private String faxNumber;
  
  @Column("EMAIL_ADDRESS")
  private String emailAddress;
  
  @Column("LOCN_EXPIRED_IND ")
  private String locnExpiredInd;
  
  @Column("RETURNED_MAIL_DATE")
  private LocalDate returnedMailDate;
  
  @Column("TRUST_LOCATION_IND")
  private String trustLocationInd;
  
  @Column("CLI_LOCN_COMMENT ")
  private String cliLocnComment;
  
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
