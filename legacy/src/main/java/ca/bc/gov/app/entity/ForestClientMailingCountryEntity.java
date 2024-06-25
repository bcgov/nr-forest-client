package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "mailing_country", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class ForestClientMailingCountryEntity {

  @Column("country_code")
  @Size(min = 0, max = 3)
  private String code;
  
  @Column("country_name")
  @NotNull
  @Size(min = 1, max = 50)
  private String name;

}
