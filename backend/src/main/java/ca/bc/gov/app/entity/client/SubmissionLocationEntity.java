package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
  @JsonIgnore
  private byte[] originalStreetAddress;

  @Transient
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
  @JsonIgnore
  private byte[] originalCityName;

  @Transient
  private String cityName;

  @Column("postal_code")
  @NotNull
  @Size(min = 5, max = 10)
  @JsonIgnore
  private byte[] originalPostalCode;

  @Transient
  private String postalCode;

  @Column("location_name")
  @NotNull
  @Size(min = 2, max = 40)
  private String name;

  @Column("business_phone_number")
  @NotNull
  @Size(min = 5, max = 14)

  @JsonIgnore
  private byte[] originalBusinessPhoneNumber;

  @Transient
  private String businessPhoneNumber;

  @Column("secondary_phone_number")
  @Size(max = 14)
  @JsonIgnore
  private byte[] originalSecondaryPhoneNumber;

  @Transient
  private String secondaryPhoneNumber;

  @Column("fax_number")
  @Size(max = 14)
  @JsonIgnore
  private byte[] originalFaxNumber;

  @Transient
  private String faxNumber;

  @Column("email_address")
  @Size(min = 5, max = 100)
  @JsonIgnore
  private byte[] originalEmailAddress;

  @Transient
  private String emailAddress;

  @Column("notes")
  @Size(max = 4000)
  private String notes;

  @Column("complementary_address_1")
  @Size(max = 40)
  @JsonIgnore
  private byte[] originalComplementaryAddress1;

  @Transient
  private String complementaryAddress1;

  @Column("complementary_address_2")
  @Size(max = 40)
  @JsonIgnore
  private byte[] originalComplementaryAddress2;

  @Transient
  private String complementaryAddress2;
  
}
