package ca.bc.gov.app.entity;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "submission_location_contact_xref", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionLocationContactEntity {

  @Column("submission_contact_id")
  @NotNull
  private Integer submissionContactId;

  @Column("submission_location_id")
  @NotNull
  private Integer submissionLocationId;
  
}
