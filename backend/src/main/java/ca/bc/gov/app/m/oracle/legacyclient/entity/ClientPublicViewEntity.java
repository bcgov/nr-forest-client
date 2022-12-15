package ca.bc.gov.app.m.oracle.legacyclient.entity;

import ca.bc.gov.app.core.CoreConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "V_CLIENT_PUBLIC", schema = CoreConstant.ORACLE_ATTRIBUTE_SCHEMA)
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class ClientPublicViewEntity {
  @Id
  @Column(name = "CLIENT_NUMBER")
  private String clientNumber;

  @Column(name = "CLIENT_NAME")
  private String clientName;

  @Column(name = "LEGAL_FIRST_NAME")
  private String legalFirstName;

  @Column(name = "LEGAL_MIDDLE_NAME")
  private String legalMiddleName;

  @Column(name = "CLIENT_STATUS_CODE")
  private String clientStatusCode;

  @Column(name = "CLIENT_TYPE_CODE")
  private String clientTypeCode;

}
