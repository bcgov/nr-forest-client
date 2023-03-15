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

@Table(name = "submission_location_contact", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionLocationContactEntity {
  @Id
  @Column("submission_location_id")
  private Integer submissionLocationContactId;

  @Column("submission_location_id")
  private Integer submissionLocationId;

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
