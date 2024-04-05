package ca.bc.gov.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Builder
public class ForestClientLocationEntityId {

  @Column("client_number")
  private String clientNumber;

  @Column("client_locn_code")
  private String clientLocnCode;
}
