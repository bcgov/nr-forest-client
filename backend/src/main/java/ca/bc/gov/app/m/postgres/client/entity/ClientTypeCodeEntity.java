package ca.bc.gov.app.m.postgres.client.entity;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import ca.bc.gov.app.core.entity.AbstractCodeDescrEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CLIENT_TYPE_CODE", schema = PostgresPersistenceConfiguration.POSTGRES_ATTRIBUTE_SCHEMA)
public class ClientTypeCodeEntity extends AbstractCodeDescrEntity {

  private static final long serialVersionUID = 8069253248355277428L;

  public static final String INDIVIDUAL = "I";
  public static final String ASSOCIATION = "A";
  public static final String CORPORATION = "C";
  public static final String FIRST_NATION_BAND = "B";
  public static final String UNREGISTERED_COMPANY = "U";


  @Id
  @Column(name = "CLIENT_TYPE_CODE")
  private String code;

  @Override
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

}
