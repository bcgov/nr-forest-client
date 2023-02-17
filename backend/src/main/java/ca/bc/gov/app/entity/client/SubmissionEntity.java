package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "submission", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionEntity {
  @Id
  @Column("submission_id")
  private Integer submissionId;

  @Column("submitter_user_guid")
  private String submitterUserId;

  @Column("submission_status_code")
  private SubmissionStatusEnum submissionStatus;

  @Column("submission_date")
  private LocalDateTime submissionDate;

  @Column("update_timestamp")
  private LocalDateTime updateTimestamp;

  @NotNull
  @Column("create_user")
  private String createUser;

  @Column("update_user")
  private String updateUser;
}
