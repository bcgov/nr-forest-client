package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import io.r2dbc.postgresql.codec.Json;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "submission_matching_detail", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionMatchDetailEntity {
  @Id
  @Column("submission_matching_detail_id")
  private Integer submissionMatchingId;

  @Column("submission_id")
  private Integer submissionId;

  @Column("matching_fields")
  private Json matchingField;

  @Column("confirmed_match_status_ind")
  private String status;

  @Column("confirmed_match_message")
  private String matchingMessage;

  @Column("confirmed_match_timestamp")
  protected LocalDateTime updatedAt;

  @Column("confirmed_match_userid")
  protected String createdBy;

  @Transient
  private Map<String,Object> matchers;
}
