package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "submission_detail", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionDetailEntity {

  @Id
  @Column("submission_detail_id")
  private Integer submissionDetailId;

  @Column("submission_id")
  @NotNull
  private Integer submissionId;
  
  @Column("client_number")
  @Size(min = 1, max = 8)
  private String clientNumber;

  @Column("incorporation_number")
  @Size(min = 1, max = 13)
  private String registrationNumber;

  @Column("organization_name")
  @Size(min = 3, max = 60)
  private String organizationName;

  @Column("business_type_code")
  @NotNull
  @Size(min = 1, max = 1)
  private String businessTypeCode;

  @Column("client_type_code")
  @NotNull
  @Size(min = 1, max = 1)
  private String clientTypeCode;

  @Column("good_standing_ind")
  @Size(min = 1, max = 1)
  private String goodStandingInd;
  
  @Column("birthdate")
  private LocalDate birthdate;
  
  @Column("district_code")
  @Size(min = 2, max = 5)
  private String districtCode;

  @Column("work_safe_bc_number")
  @Size(max = 6)
  private String workSafeBCNumber;

  @Column("doing_business_as")
  @Size(max = 120)
  private String doingBusinessAs;

  @Column("client_acronym")
  @Size(max = 8)
  private String clientAcronym;

  @Column("first_name")
  @Size(max = 30)
  private String firstName;

  @Column("middle_name")
  @Size(max = 30)
  private String middleName;

  @Column("last_name")
  @Size(max = 30)
  private String lastName;

  @Column("notes")
  @Size(max = 4000)
  private String notes;

  @Column("identification_type_code")
  @Size(max = 4)
  private String identificationTypeCode;

  @Column("client_identification")
  @Size(max = 40)
  private String clientIdentification;

  @Column("identification_country_code")
  @Size(max = 2)
  private String countryCode;

  @Column("identification_province_code")
  @Size(max = 4)
  private String provinceCode;
  
}
