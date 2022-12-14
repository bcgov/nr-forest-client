package ca.bc.gov.app.m.oracle.legacyclient.entity;

import ca.bc.gov.app.core.configuration.OraclePersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "CLIENT_STATUS_CODE", schema = OraclePersistenceConfiguration.ORACLE_ATTRIBUTE_SCHEMA)
public class ClientStatusCodeEntity implements AbstractEntity {

  private static final long serialVersionUID = 4341025008217142732L;

  public static final String ACTIVE = "ACT";

  @Id
  @Column(name = "CLIENT_STATUS_CODE")
  private String clientStatusCode;

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

  public String getClientStatusCode() {
    return clientStatusCode;
  }

  public void setClientStatusCode(String clientStatusCode) {
    this.clientStatusCode = clientStatusCode;
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