package ca.bc.gov.app.m.postgres.client.entity;

import ca.bc.gov.app.core.CoreConstant;
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
@Table(name = "CLIENT_TYPE_CODE", schema = CoreConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class ClientTypeCodeEntity {


  public static final String INDIVIDUAL = "I";
  public static final String ASSOCIATION = "A";
  public static final String CORPORATION = "C";
  public static final String FIRST_NATION_BAND = "B";
  public static final String UNREGISTERED_COMPANY = "U";


  @Id
  @Column(name = "CLIENT_TYPE_CODE")
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
