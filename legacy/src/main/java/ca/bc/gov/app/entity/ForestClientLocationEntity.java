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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "client_location", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientLocationEntity {

  @Id
  @Column("client_number")
  @Size(min = 1, max = 8)
  private String clientNumber;
  
  @Column("client_locn_code")
  @NotNull
  @Size(min = 1, max = 2)
  private String clientLocnCode;
  
  @Column("client_locn_name")
  @Size(min = 1, max = 40)
  private String clientLocnName;
  
  @Column("hdbs_company_code")
  @Size(min = 1, max = 5)
  private String hdbsCompanyCode;
  
  @Column("address_1")
  @NotNull
  @Size(min = 1, max = 40)
  private String addressOne;
  
  @Column("address_2")
  @Size(min = 1, max = 40)
  private String addressTwo;
  
  @Column("address_3")
  @Size(min = 1, max = 40)
  private String addressThree;
  
  @Column("city")
  @NotNull
  @Size(min = 1, max = 30)
  private String city;
  
  @Column("province")
  @Size(min = 1, max = 50)
  private String province;
  
  @Column("postal_code")
  @Size(min = 1, max = 10)
  private String postalCode;
  
  @Column("country")
  @NotNull
  @Size(min = 1, max = 50)
  private String country;
  
  @Column("business_phone")
  @Size(min = 1, max = 10)
  private String businessPhone;
  
  @Column("home_phone")
  @Size(min = 1, max = 10)
  private String homePhone;
  
  @Column("cell_phone")
  @Size(min = 1, max = 10)
  private String cellPhone;
  
  @Column("fax_number")
  @Size(min = 1, max = 10)
  private String faxNumber;
  
  @Column("email_address")
  @Size(min = 1, max = 128)
  private String emailAddress;
  
  @Column("locn_expired_ind")
  @NotNull
  @Size(min = 1, max = 1)
  private String locnExpiredInd;
  
  @Column("returned_mail_date")
  private LocalDate returnedMailDate;
  
  @Column("trust_location_ind")
  @NotNull
  @Size(min = 1, max = 1)
  private String trustLocationInd;
  
  @Column("cli_locn_comment")
  @Size(min = 1, max = 4000)
  private String cliLocnComment;
  
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
  private Long updatedByUnit;
  
  @Column("add_org_unit")
  @NotNull
  private Long createdByUnit;
  
  @Column("revision_count")
  @NotNull
  private Long revision;

}
