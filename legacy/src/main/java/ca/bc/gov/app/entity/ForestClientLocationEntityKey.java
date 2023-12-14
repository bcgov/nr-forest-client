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
public class ForestClientLocationEntityKey {

  @Column("CLIENT_NUMBER")
  private String clientNumber;

  @Column("CLIENT_LOCN_CODE")
  private String clientLocnCode;
}
