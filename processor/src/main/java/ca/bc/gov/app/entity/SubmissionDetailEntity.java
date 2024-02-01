package ca.bc.gov.app.entity;

import ca.bc.gov.app.ApplicationConstant;
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
  private Integer submissionId;

  @Column("client_number")
  private String clientNumber;

  @Column("incorporation_number")
  private String incorporationNumber;

  @Column("organization_name")
  private String organizationName;

  @Column("business_type_code")
  private String businessTypeCode;

  @Column("client_type_code")
  private String clientTypeCode;

  @Column("good_standing_ind")
  private String goodStandingInd;

  @Column("birthdate")
  private LocalDate birthdate;

  @Column("district_id")
  private String districtId;
}
