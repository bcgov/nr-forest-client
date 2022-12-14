package ca.bc.gov.app.m.postgres.client.entity;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractEntity;
import ca.bc.gov.app.core.misc.scope.ScopeConstant;
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
import java.util.Objects;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "SUBMISSION_DETAIL", schema = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA)
@Component(SubmissionDetailEntity.BEAN_NAME)
@Scope(ScopeConstant.PROTOTYPE)
public class SubmissionDetailEntity implements AbstractEntity {

  private static final long serialVersionUID = -8966248654104607818L;

  public static final String BEAN_NAME = "submissionDetailEntity";

  @Id
  @GeneratedValue(generator = "SEQ_SUBMISSION_DETAIL")
  @SequenceGenerator(name = "SEQ_SUBMISSION_DETAIL",
      sequenceName = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA_QUALIFIER +
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

  public Long getSubmissionDetailId() {
    return submissionDetailId;
  }

  public void setSubmissionDetailId(Long submissionDetailId) {
    this.submissionDetailId = submissionDetailId;
  }

  public SubmissionEntity getSubmissionEntity() {
    return submissionEntity;
  }

  public void setSubmissionEntity(SubmissionEntity submissionEntity) {
    this.submissionEntity = submissionEntity;
  }

  public String getIncorporationNumber() {
    return incorporationNumber;
  }

  public void setIncorporationNumber(String incorporationNumber) {
    this.incorporationNumber = incorporationNumber;
  }

  public Date getOrganizationName() {
    return organizationName;
  }

  public void setOrganizationName(Date organizationName) {
    this.organizationName = organizationName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public ClientTypeCodeEntity getClientTypeCodeEntity() {
    return clientTypeCodeEntity;
  }

  public void setClientTypeCodeEntity(ClientTypeCodeEntity clientTypeCodeEntity) {
    this.clientTypeCodeEntity = clientTypeCodeEntity;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getClientComment() {
    return clientComment;
  }

  public void setClientComment(String clientComment) {
    this.clientComment = clientComment;
  }

  @Override
  public int hashCode() {
    return Objects.hash(submissionDetailId);
  }

  @Override
  public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
    SubmissionDetailEntity other = (SubmissionDetailEntity) obj;
    return Objects.equals(submissionDetailId, other.submissionDetailId);
  }

}
