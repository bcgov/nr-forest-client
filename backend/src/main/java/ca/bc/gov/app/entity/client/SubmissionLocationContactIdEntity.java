package ca.bc.gov.app.entity.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionLocationContactIdEntity {

  @Column("submission_contact_id")
  private Integer submissionContactId;

  @Column("submission_location_id")
  private Integer submissionLocationId;
  
}
