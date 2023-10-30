package ca.bc.gov.app.entity.legacy;

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
public class ClientTypeCompanyXrefEntityKey {

  @Column("CLIENT_TYPE_CODE")
  private String clientTypeCode;
  @Column("REGISTRY_COMPANY_TYPE_CODE")
  private String registryCompanyTypeCode;

}
