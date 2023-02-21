package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "submission", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionEntity extends BaseEntity {
  @Id
  @Column("submission_id")
  private Integer submissionId;

  @Column("submitter_user_guid")
  private String submitterUserId;

  @Column("submission_status_code")
  private SubmissionStatusEnum submissionStatus;

  @Column("submission_date")
  @Builder.Default
  private LocalDateTime submissionDate = LocalDateTime.now();

}
