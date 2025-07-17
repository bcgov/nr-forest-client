package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@With
@Table(name = "CLIENT_RELATIONSHIP_CODE", schema = ORACLE_ATTRIBUTE_SCHEMA) 
public class ClientRelationshipCodeEntity extends ExpirableBaseEntity {

  @Id
  @Column("CLIENT_RELATIONSHIP_CODE")
  @NotNull
  @Size(min = 1, max = 2)
  private String code;

}
