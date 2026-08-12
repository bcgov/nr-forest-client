package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import java.time.LocalDate;
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
@Table(name = "SCALE_SITE_CONTACT", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ScaleSiteContactEntity {

  @Id
  @Column("CLIENT_CONTACT_ID")
  private Long clientContactId;

  @Column("SCALE_SITE_ID_NMBR")
  @NotNull
  @Size(min = 1, max = 4)
  private String scaleSiteIdNumber;

  @Column("CONTACT_ROLE_DESCRIPTION")
  @NotNull
  @Size(min = 1, max = 40)
  private String contactRoleDescription;

  @Column("PRIMARY_CONTACT_IND")
  @NotNull
  @Size(min = 1, max = 1)
  private String primaryContactInd;

  @Column("SITE_INFORMATION_ACCESS_IND")
  @NotNull
  @Size(min = 1, max = 1)
  private String siteInformationAccessInd;

  @Column("EFFECTIVE_DATE")
  @NotNull
  private LocalDate effectiveDate;

  @Column("EXPIRY_DATE")
  private LocalDate expiryDate;

  @Column("ENTRY_TIMESTAMP")
  @NotNull
  private LocalDateTime createdAt;

  @Column("ENTRY_USERID")
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

  @Column("REVISION_COUNT")
  @NotNull
  private Long revision;

}
