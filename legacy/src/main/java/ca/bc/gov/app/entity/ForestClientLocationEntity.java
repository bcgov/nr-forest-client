package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;
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
@Table(name = "client_location", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientLocationEntity {

  @Id
  @Column("client_number")
  private String clientNumber;
  
  @Column("client_locn_code")
  private String clientLocnCode;
  
  @Column("client_locn_name")
  private String clientLocnName;
  
  @Column("hdbs_company_code")
  private String hdbsCompanyCode;
  
  @Column("address_1")
  private String addressOne;
  
  @Column("address_2")
  private String addressTwo;
  
  @Column("address_3")
  private String addressThree;
  
  @Column("city")
  private String city;
  
  @Column("province")
  private String province;
  
  @Column("postal_code")
  private String postalCode;
  
  @Column("country")
  private String country;
  
  @Column("business_phone")
  private String businessPhone;
  
  @Column("home_phone")
  private String homePhone;
  
  @Column("cell_phone")
  private String cellPhone;
  
  @Column("fax_number")
  private String faxNumber;
  
  @Column("email_address")
  private String emailAddress;
  
  @Column("locn_expired_ind ")
  private String locnExpiredInd;
  
  @Column("returned_mail_date")
  private LocalDate returnedMailDate;
  
  @Column("trust_location_ind")
  private String trustLocationInd;
  
  @Column("cli_locn_comment")
  private String cliLocnComment;
  
  @Column("add_timestamp")
  private LocalDateTime createdAt;
  
  @Column("add_userid")
  private String createdBy;
  
  @Column("update_timestamp")
  private LocalDateTime updatedAt;
  
  @Column("update_userid")
  private String updatedBy;
  
  @Column("update_org_unit")
  private Long updatedByUnit;
  
  @Column("add_org_unit")
  private Long createdByUnit;
  
  @Column("revision_count")
  private Long revision;

}
