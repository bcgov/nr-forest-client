package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "v_client_public", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ClientPublicViewEntity {

  @Id
  @Column("client_number")
  private String clientNumber;

  @Column("client_name")
  private String clientName;

  @Column("legal_first_name")
  private String legalFirstName;

  @Column("legal_middle_name")
  private String legalMiddleName;

  @Column("client_status_code")
  private String clientStatusCode;

  @Column("client_type_code")
  private String clientTypeCode;

}