package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "province_code", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class ProvinceCodeEntity extends BaseEntity {

  @Id
  @Column("province_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String provinceCode;

  @Column("country_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String countryCode;

}
