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


@Table(name = "client_type_code", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class ClientTypeCodeEntity extends ExpirableBaseEntity {

  @Id
  @Column("client_type_code")
  private String code;

  public ClientTypeCodeEntity(
      @NotNull @Size(min = 1, max = 4) String code,
      @NotNull String description) {
    this.code = code;
    this.description = description;
  }
  
}
