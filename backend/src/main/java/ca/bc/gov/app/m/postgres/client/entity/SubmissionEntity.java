package ca.bc.gov.app.m.postgres.client.entity;

import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractEntity;
import ca.bc.gov.app.core.misc.scope.ScopeConstant;

@Entity
@Table(name = "SUBMISSION", schema = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA)
@Component(SubmissionEntity.BEAN_NAME)
@Scope(ScopeConstant.PROTOTYPE)
public class SubmissionEntity implements AbstractEntity {

	private static final long serialVersionUID = -8966248654104607818L;

	public static final String BEAN_NAME = "submissionEntity";

	@Id
	@GeneratedValue(generator = "SEQ_SUBMISSION")
	@SequenceGenerator(name = "SEQ_SUBMISSION", 
					   sequenceName = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA_QUALIFIER + "SUBMISSION_ID_SEQ", 
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

	public Long getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(Long submissionId) {
		this.submissionId = submissionId;
	}

	public String getSubmitterUserGuid() {
		return submitterUserGuid;
	}

	public void setSubmitterUserGuid(String submitterUserGuid) {
		this.submitterUserGuid = submitterUserGuid;
	}

	public SubmissionStatusCodeEntity getSubmissionStatusCodeEntity() {
		return submissionStatusCodeEntity;
	}

	public void setSubmissionStatusCodeEntity(SubmissionStatusCodeEntity submissionStatusCodeEntity) {
		this.submissionStatusCodeEntity = submissionStatusCodeEntity;
	}

	public Date getSubmitterDate() {
		return submitterDate;
	}

	public void setSubmitterDate(Date submitterDate) {
		this.submitterDate = submitterDate;
	}

	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	public void setUpdateTimestamp(Date updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Override
	public int hashCode() {
		return Objects.hash(submissionId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubmissionEntity other = (SubmissionEntity) obj;
		return Objects.equals(submissionId, other.submissionId);
	}

}
