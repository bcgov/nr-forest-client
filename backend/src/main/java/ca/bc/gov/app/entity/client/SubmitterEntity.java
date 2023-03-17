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

@Table(name = "submission_submitter", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmitterEntity {
  @Id
  @Column("submission_submitter_id")
  private Integer submissionSubmitterId;

  @Column("submission_id")
  private Integer submissionId;

  @Column("first_name")
  private String firstName;

  @Column("last_name")
  private String lastName;

  @Column("phone_number")
  private String phoneNumber;

  @Column("email_address")
  private String emailAddress;
}
