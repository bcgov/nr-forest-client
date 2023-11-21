package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
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

  public static final String INDIVIDUAL = "I";
  public static final String REGISTERED_SOLE_PROPRIETORSHIP = "RSP";
  public static final String UNREGISTERED_SOLE_PROPRIETORSHIP = "USP";
  
  @Id
  @Column("client_type_code")
  private String code;

}
