package ca.bc.gov.app.entity.client;


import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "submission_detail", schema = "nrfc")
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

  @Column("incorporation_number")
  private String incorporationNumber;

  @Column("organization_name")
  private String organizationName;

  @Column("first_name")
  private String firstName;

  @Column("middle_name")
  private String middleName;

  @Column("last_name")
  private String lastName;

  @Column("client_type_code")
  private String clientTypeCode;

  @Column("date_of_birth")
  private LocalDate dateOfBirth;

  @Column("doing_business_as_ind")
  private String doingBusinessAsInd;

  @Column("doing_business_as_name")
  private String doingBusinessAsName;

  @Column("has_additional_location_ind")
  private String hasAdditionalLocationInd;

  @Transient
  public String getDisplayName(){
    if(StringUtils.defaultString(clientTypeCode).equalsIgnoreCase("I")){
      return
          StringUtils.joinWith(" ",
              StringUtils.defaultString(firstName),
              StringUtils.defaultString(middleName),
              StringUtils.defaultString(lastName)
          );
    }
    return StringUtils.defaultString(organizationName);
  }

}
