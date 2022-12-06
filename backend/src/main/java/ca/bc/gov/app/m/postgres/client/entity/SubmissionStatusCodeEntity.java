package ca.bc.gov.app.m.postgres.client.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractCodeDescrEntity;
import ca.bc.gov.app.core.misc.scope.ScopeConstant;

@Entity
@Table(name = "SUBMISSION_STATUS_CODE", schema = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA)
@Component(SubmissionStatusCodeEntity.BEAN_NAME)
@Scope(ScopeConstant.PROTOTYPE)
public class SubmissionStatusCodeEntity extends AbstractCodeDescrEntity {

	private static final long serialVersionUID = 4341025008217142732L;

	public static final String BEAN_NAME = "submissionStatusCodeEntity";
	
	public static final String IN_PROGRESS					= "P";
	public static final String APPROVED						= "A";
	public static final String REJECTED						= "R";
	public static final String DELETED						= "E";
	public static final String SUBMITTED					= "S";

	@Id
	@Column(name = "SUBMISSION_STATUS_CODE")
	private String code;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
