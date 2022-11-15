package ca.bc.gov.app.m.postgres.client.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractEntity;
import ca.bc.gov.app.core.misc.scope.ScopeConstant;

@Entity
@Table(name = "SUBMISSION_STATUS_CODE", schema = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA)
@Component(SubmissionStatusCodeEntity.BEAN_NAME)
@Scope(ScopeConstant.PROTOTYPE)
public class SubmissionStatusCodeEntity implements AbstractEntity {

	private static final long serialVersionUID = 4341025008217142732L;

	public static final String BEAN_NAME = "submissionStatusCodeEntity";
	
	public static final String IN_PROGRESS					= "P";
	public static final String APPROVED						= "A";
	public static final String REJECTED						= "R";
	public static final String DELETED						= "E";
	public static final String SUBMITTED					= "S";

	@Id
	@Column(name = "SUBMISSION_STATUS_CODE")
	private String submissionStatusCode;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "EFFECTIVE_DATE")
	private Date effectiveDate;

	@Column(name = "EXPIRY_DATE")
	private Date expiryDate;

	@Column(name = "CREATE_TIMESTAMP")
	private Date createTimestamp;

	@Column(name = "UPDATE_TIMESTAMP")
	private Date updateTimestamp;

	@Column(name = "CREATE_USER")
	private String createUser;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	public String getSubmissionStatusCode() {
		return submissionStatusCode;
	}

	public void setSubmissionStatusCode(String submissionStatusCode) {
		this.submissionStatusCode = submissionStatusCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Date createTimestamp) {
		this.createTimestamp = createTimestamp;
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


	
	

}
