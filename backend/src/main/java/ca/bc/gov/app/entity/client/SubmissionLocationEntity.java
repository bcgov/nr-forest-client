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

@Table(name = "submission_location", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubmissionLocationEntity {
  
  @Id
  @Column("submission_location_id")
  private Integer submissionLocationId;

  @Column("submission_id")
  @NotNull
  private Integer submissionId;

  @Column("street_address")
  @NotNull
  @Size(min = 3, max = 50)
  private String streetAddress;

  @Column("country_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String countryCode;

  @Column("province_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String provinceCode;

  @Column("city_name")
  @NotNull
  @Size(min = 2, max = 100)
  private String cityName;

  @Column("postal_code")
  @NotNull
  @Size(min = 5, max = 10)
  private String postalCode;

  @Column("location_name")
  @NotNull
  @Size(min = 2, max = 20)
  private String name;
  
}
