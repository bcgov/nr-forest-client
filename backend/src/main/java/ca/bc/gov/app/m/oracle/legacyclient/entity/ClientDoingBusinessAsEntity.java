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
@Table(name = "CLIENT_DOING_BUSINESS_AS", schema = CoreConstant.ORACLE_ATTRIBUTE_SCHEMA)
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class ClientDoingBusinessAsEntity {

  @Id
  @Column(name = "CLIENT_NUMBER")
  private String clientNumber;

  @Column(name = "DOING_BUSINESS_AS_NAME")
  private String doingBusinessAsName;

}
