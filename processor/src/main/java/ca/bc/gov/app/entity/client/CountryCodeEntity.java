package ca.bc.gov.app.entity.client;


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

@Table(name = "country_code", schema = "nrfc")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class CountryCodeEntity extends ExpirableBaseEntity {
  @Id
  @Column("country_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String countryCode;

  @Column("display_order")
  @NotNull
  private Integer order;

}
