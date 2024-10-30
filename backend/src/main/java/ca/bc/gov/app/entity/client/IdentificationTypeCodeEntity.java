package ca.bc.gov.app.entity.client;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ca.bc.gov.app.ApplicationConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Table(name = "identification_type_code", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class IdentificationTypeCodeEntity extends ExpirableBaseEntity {

  @Id
  @Column("identification_type_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String code;

  @Column("country_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String countryCode;

}
