package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "MAILING_COUNTRY", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientMailingCountryEntity {

  @Column("COUNTRY_NAME")
  private String name;

  @Column("COUNTRY_CODE")
  private String code;

}
