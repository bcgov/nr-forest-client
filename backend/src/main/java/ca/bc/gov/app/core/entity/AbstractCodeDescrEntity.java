package ca.bc.gov.app.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
public abstract class AbstractCodeDescrEntity implements AbstractEntity, CodeDescr {

  private static final long serialVersionUID = 4380176116164211795L;

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
