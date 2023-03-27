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
@Table(name = "CLIENT_DOING_BUSINESS_AS", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientDoingBusinessAsEntity {

  @Id
  @Column("CLIENT_NUMBER")
  private String clientNumber;

  @Column("DOING_BUSINESS_AS_NAME")
  private String doingBusinessAsName;

}
