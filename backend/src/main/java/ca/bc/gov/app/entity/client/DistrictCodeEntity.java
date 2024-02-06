package ca.bc.gov.app.entity.client;

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
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "district_code", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class DistrictCodeEntity extends ExpirableBaseEntity {

  @Id
  @Column("district_code")
  @NotNull
  @Size(min = 2, max = 5)
  private String districtCode;
  
  @Column("email_address")
  @NotNull
  private String emailAddress;

  public DistrictCodeEntity(
      @NotNull @Size(min = 2, max = 5) String districtCode,
      @NotNull String description,
      @NotNull String emailAddress) {
    this.districtCode = districtCode;
    this.description = description;
    this.emailAddress = emailAddress;
  }

}
