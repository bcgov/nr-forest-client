package ca.bc.gov.app.m.postgres.client.entity;

import ca.bc.gov.app.core.CoreConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "SUBMISSION", schema = CoreConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionEntity {
  @Id
  @GeneratedValue(generator = "SEQ_SUBMISSION")
  @SequenceGenerator(name = "SEQ_SUBMISSION",
      sequenceName = CoreConstant.POSTGRES_ATTRIBUTE_SCHEMA_QUALIFIER +
          "SUBMISSION_ID_SEQ",
      allocationSize = 1)
  @Column(name = "SUBMISSION_ID", nullable = false, precision = 10, scale = 0)
  private Long submissionId;

  @Column(name = "SUBMITTER_USER_GUID")
  private String submitterUserGuid;

  @ManyToOne
  @JoinColumn(name = "SUBMISSION_STATUS_CODE")
  private SubmissionStatusCodeEntity submissionStatusCodeEntity;

  @Column(name = "SUBMISSION_DATE")
  private Date submitterDate;

  @Column(name = "UPDATE_TIMESTAMP")
  private Date updateTimestamp;

  @Column(name = "CREATE_USER")
  private String createUser;

  @Column(name = "UPDATE_USER")
  private String updateUser;

}
