package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
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

@Table(name = "submission_contact", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionContactEntity {
  
  @Id
  @Column("submission_contact_id")
  private Integer submissionContactId;

  @Column("submission_id")
  @NotNull
  private Integer submissionId;

  @Column("contact_type_code")
  @NotNull
  @Size(min = 1, max = 2)
  private String contactTypeCode;

  @Column("first_name")
  @Size(min = 1, max = 30)
  private String firstName;

  @Column("last_name")
  @Size(min = 1, max = 30)
  private String lastName;

  @Column("business_phone_number")
  @NotNull
  @Size(min = 5, max = 14)
  private String businessPhoneNumber;

  @Column("secondary_phone_number")
  @Size(max = 14)
  private String secondaryPhoneNumber;

  @Column("fax_number")
  @Size(max = 14)
  private String faxNumber;

  @Column("email_address")
  @NotNull
  @Size(min = 5, max = 100)
  private String emailAddress;

  @Column("idp_user_id")
  @Size(min = 5, max = 50)
  private String userId;
  
}
