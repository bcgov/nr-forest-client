package ca.bc.gov.app.m.oracle.legacyclient.entity;

import ca.bc.gov.app.core.configuration.OraclePersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "CLIENT_DOING_BUSINESS_AS", schema = OraclePersistenceConfiguration.ORACLE_ATTRIBUTE_SCHEMA)
public class ClientDoingBusinessAsEntity implements AbstractEntity {

  private static final long serialVersionUID = 608865729103878585L;

  @Id
  @Column(name = "CLIENT_NUMBER")
  private String clientNumber;

  @Column(name = "DOING_BUSINESS_AS_NAME")
  private String doingBusinessAsName;

  public String getClientNumber() {
    return clientNumber;
  }

  public void setClientNumber(String clientNumber) {
    this.clientNumber = clientNumber;
  }

  public String getDoingBusinessAsName() {
    return doingBusinessAsName;
  }

  public void setDoingBusinessAsName(String doingBusinessAsName) {
    this.doingBusinessAsName = doingBusinessAsName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientNumber);
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
    ClientDoingBusinessAsEntity other = (ClientDoingBusinessAsEntity) obj;
    return Objects.equals(clientNumber, other.clientNumber);
  }

}
