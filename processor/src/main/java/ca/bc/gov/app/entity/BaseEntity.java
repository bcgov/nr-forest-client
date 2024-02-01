package ca.bc.gov.app.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * The type Base entity.
 * Contains base entity fields
 */
public abstract class BaseEntity {

  @Column("update_timestamp")
  protected LocalDateTime updatedAt;

  @Column("create_user")
  protected String createdBy;

  @Column("update_user")
  protected String updatedBy;
}
