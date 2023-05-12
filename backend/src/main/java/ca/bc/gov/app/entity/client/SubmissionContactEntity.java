package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
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
  private Integer submissionId;

  @Column("contact_type_code")
  private String contactTypeCode;

  @Column("first_name")
  private String firstName;

  @Column("last_name")
  private String lastName;

  @Column("business_phone_number")
  private String businessPhoneNumber;

  @Column("email_address")
  private String emailAddress;
}
