package ca.bc.gov.app.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
/**
 * The type Expirable base entity.
 * @param <T> the type parameter
 *           the type parameter
 *
 * Contains description and effective/expiry dates
 */
public abstract class ExpirableBaseEntity extends BaseEntity {

  @NotNull
  @Size(min = 5, max = 100)
  protected String description;

  @Column("create_timestamp")
  protected LocalDateTime createdAt;

  @NotNull
  @Column("effective_date")
  protected LocalDate effectiveAt;

  @NotNull
  @Column("expiry_date")
  protected LocalDate expiredAt;
  
}
