package ca.bc.gov.app.entity.client;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import jakarta.validation.constraints.NotNull;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

  @Column("update_timestamp")
  protected LocalDateTime updatedAt;

  @Column("create_user")
  @NotNull
  protected String createdBy;

  @Column("update_user")
  protected String updatedBy;
  
}
