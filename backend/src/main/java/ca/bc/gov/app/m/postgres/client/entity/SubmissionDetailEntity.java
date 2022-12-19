package ca.bc.gov.app.m.postgres.client.entity;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "SUBMISSION_DETAIL", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionDetailEntity {

  @Id
  @GeneratedValue(generator = "SEQ_SUBMISSION_DETAIL")
  @SequenceGenerator(name = "SEQ_SUBMISSION_DETAIL",
      sequenceName = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA_QUALIFIER +
          "SUBMISSION_DETAIL_ID_SEQ",
      allocationSize = 1)
  @Column(name = "SUBMISSION_DETAIL_ID", nullable = false, precision = 10, scale = 0)
  private Long submissionDetailId;

  @OneToOne
  @JoinColumn(name = "SUBMISSION_ID")
  private SubmissionEntity submissionEntity;

  @Column(name = "INCORPORATION_NUMBER")
  private String incorporationNumber;

  @Column(name = "ORGANIZATION_NAME")
  private Date organizationName;

  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "MIDDLE_NAME")
  private String middleName;

  @Column(name = "LAST_NAME")
  private String lastName;

  @ManyToOne
  @JoinColumn(name = "CLIENT_TYPE_CODE")
  private ClientTypeCodeEntity clientTypeCodeEntity;

  @Column(name = "DATE_OF_BIRTH")
  private Date dateOfBirth;

  @Column(name = "CLIENT_COMMENT")
  private String clientComment;

}
