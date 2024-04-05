package ca.bc.gov.app.entity;


import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "forest_client", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientEntity {

  @Id
  @Column("client_number")
  @Size(min = 1, max = 8)
  private String clientNumber;
  
  @Column("client_name")
  @NotNull
  @Size(min = 1, max = 60)
  private String clientName;
  
  @Column("legal_first_name")
  private String legalFirstName;
  
  @Column("legal_middle_name")
  private String legalMiddleName;
  
  @Column("client_status_code")
  @NotNull
  @Size(min = 1, max = 3)
  private String clientStatusCode;
  
  @Column("client_type_code")
  @NotNull
  @Size(min = 1, max = 1)
  private String clientTypeCode;
  
  @Column("birthdate")
  private LocalDateTime birthdate;
  
  @Column("client_id_type_code")
  @Size(min = 1, max = 4)
  private String clientIdTypeCode;
  
  @Column("client_identification")
  @Size(min = 1, max = 40)
  private String clientIdentification;
  
  @Column("registry_company_type_code")
  @Size(min = 1, max = 4)
  private String registryCompanyTypeCode;
  
  @Column("corp_regn_nmbr")
  @Size(min = 1, max = 9)
  private String corpRegnNmbr;
  
  @Column("client_acronym")
  @Size(min = 1, max = 8)
  private String clientAcronym;
  
  @Column("wcb_firm_number")
  @Size(min = 1, max = 6)
  private String wcbFirmNumber;
  
  @Column("ocg_supplier_nmbr")
  @Size(min = 1, max = 10)
  private String ocgSupplierNmbr;
  
  @Column("client_comment")
  @Size(min = 1, max = 4000)
  private String clientComment;
  
  @Column("add_timestamp")
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

  @Transient
  public String getName() {
    if (Objects.equals(this.clientTypeCode, "I")) {
      return Stream.of(this.legalFirstName, this.legalMiddleName, this.clientName)
          .filter(Objects::nonNull).collect(Collectors.joining(" "));
    } else {
      return this.clientName;
    }
  }

}
