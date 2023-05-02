package ca.bc.gov.app.entity.client;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "submission_location", schema = "nrfc")
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
  private Integer submissionId;

  @Column("street_address")
  private String streetAddress;

  @Column("country_code")
  private String countryCode;

  @Column("province_code")
  private String provinceCode;

  @Column("city_name")
  private String cityName;

  @Column("postal_code")
  private String postalCode;

  @Column("main_address_ind")
  private String mainAddressInd;
}
