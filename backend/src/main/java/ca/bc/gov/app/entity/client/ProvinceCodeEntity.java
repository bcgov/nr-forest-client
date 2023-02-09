package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class ProvinceCodeEntity {

  @Id
  @Column("province_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String provinceCode;

  @Column("country_code")
  @NotNull
  @Size(min = 2, max = 2)
  private String countryCode;

  @NotNull
  @Max(100)
  private String description;

  @Column("effective_date")
  @NotNull
  private LocalDate effectiveDate;

  @Column("expiry_date")
  @NotNull
  private LocalDate expiryDate;

  @Column("create_timestamp")
  @NotNull
  private LocalDateTime createdAt;

  @Column("update_timestamp")
  @NotNull
  private LocalDateTime updatedAt;

  @Column("create_user")
  @NotNull
  private String createdBy;

  @Column("update_user")
  private String updatedBy;
}
