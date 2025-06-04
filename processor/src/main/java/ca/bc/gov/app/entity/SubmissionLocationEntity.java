package ca.bc.gov.app.entity;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
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
  @Size(min = 3, max = 40)
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
  @Size(min = 2, max = 30)
  private String cityName;

  @Column("postal_code")
  @NotNull
  @Size(min = 5, max = 10)
  private String postalCode;

  @Column("location_name")
  @NotNull
  @Size(min = 2, max = 40)
  private String name;

  @Column("business_phone_number")
  @NotNull
  @Size(min = 5, max = 14)
  private String businessPhoneNumber;

  @Column("secondary_phone_number")
  @Size(max = 14)
  private String secondaryPhoneNumber;

  @Column("fax_number")
  @Size(max = 14)
  private String faxNumber;

  @Column("email_address")
  @Size(min = 5, max = 100)
  private String emailAddress;

  @Column("notes")
  @Size(max = 4000)
  private String notes;

  @Column("complementary_address_1")
  @Size(max = 40)
  private String complementaryAddress1;

  @Column("complementary_address_2")
  @Size(max = 40)
  private String complementaryAddress2;

  public String getAddressValue1() {
    return getAddressValue(0);
  }

  public String getAddressValue2() {
    return getAddressValue(1);
  }

  public String getAddressValue3() {
    return getAddressValue(2);
  }

  private String getAddressValue(int index) {
    return Stream.of(
            this.streetAddress,
            this.complementaryAddress1,
            this.complementaryAddress2
        )
        .filter(StringUtils::isNotBlank)
        .skip(index)
        .map(s -> s.toUpperCase(Locale.ROOT))
        .findFirst()
        .orElse(StringUtils.EMPTY);
  }
  
}
