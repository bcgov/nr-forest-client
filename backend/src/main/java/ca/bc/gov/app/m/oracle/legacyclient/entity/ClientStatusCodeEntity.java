package ca.bc.gov.app.m.oracle.legacyclient.entity;

import ca.bc.gov.app.core.CoreConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "CLIENT_STATUS_CODE", schema = CoreConstant.ORACLE_ATTRIBUTE_SCHEMA)
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatusCodeEntity {

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


}