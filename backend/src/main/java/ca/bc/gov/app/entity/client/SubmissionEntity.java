package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.SubmissionStatusEnum;
import ca.bc.gov.app.dto.client.SubmissionTypeCodeEnum;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
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

  @Column("submission_status_code")
  @Size(min = 1, max = 5)
  private SubmissionStatusEnum submissionStatus;

  @Column("submission_type_code")
  @Size(min = 3, max = 5)
  private SubmissionTypeCodeEnum submissionType;

  @Column("submission_date")
  private LocalDateTime submissionDate;
  
  @Column("update_timestamp")
  private LocalDateTime updateTimestamp;
  
}
