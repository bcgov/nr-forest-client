package ca.bc.gov.app.m.postgres.client.entity;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "SUBMISSION_STATUS_CODE", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionStatusCodeEntity {

  public static final String IN_PROGRESS = "P";
  public static final String APPROVED = "A";
  public static final String REJECTED = "R";
  public static final String DELETED = "E";
  public static final String SUBMITTED = "S";

  @Id
  @Column(name = "SUBMISSION_STATUS_CODE")
  private String code;

  @Column(name = "DESCRIPTION", nullable = false)
  private String description;

  @Temporal(TemporalType.DATE)
  @Column(name = "EFFECTIVE_DATE", nullable = false)
  private Date effectiveDate;

  @Temporal(TemporalType.DATE)
  @Column(name = "EXPIRY_DATE", nullable = true)
  private Date expiryDate;

  @Column(name = "CREATE_TIMESTAMP")
  private Date createTimestamp;

  @Column(name = "UPDATE_TIMESTAMP")
  private Date updateTimestamp;

  @Column(name = "CREATE_USER")
  private String createUser;

  @Column(name = "UPDATE_USER")
  private String updateUser;

}
