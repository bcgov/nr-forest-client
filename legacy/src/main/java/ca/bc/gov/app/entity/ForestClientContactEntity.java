package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "client_contact", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientContactEntity {

  @Id
  @Column("client_contact_id")
  private Long clientContactId;
  
  @Column("client_number")
  @NotNull
  @Size(min = 1, max = 8)
  private String clientNumber;
  
  @Column("client_locn_code")
  @NotNull
  @Size(min = 1, max = 2)
  private String clientLocnCode;
  
  @Column("bus_contact_code")
  @NotNull
  @Size(min = 1, max = 3)
  private String contactCode;
  
  @Column("contact_name")
  @NotNull
  @Size(min = 1, max = 120)
  private String contactName;
  
  @Column("business_phone")
  @Size(min = 1, max = 10)
  private String businessPhone;
  
  @Column("cell_phone")
  @Size(min = 1, max = 10)
  private String cellPhone;
  
  @Column("fax_number")
  @Size(min = 1, max = 10)
  private String faxNumber;
  
  @Column("email_address")
  @Size(min = 1, max = 128)
  private String emailAddress;
  
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
