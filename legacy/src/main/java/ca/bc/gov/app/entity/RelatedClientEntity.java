package ca.bc.gov.app.entity;

import static ca.bc.gov.app.ApplicationConstants.ORACLE_ATTRIBUTE_SCHEMA;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "related_client", schema = ORACLE_ATTRIBUTE_SCHEMA)
public class RelatedClientEntity {

  @Id
  @Column("CLIENT_NUMBER")
  @Size(min = 8, max = 8)
  private String clientNumber;

  @Column("CLIENT_LOCN_CODE")
  @Size(min = 2, max = 2)
  private String clientLocationCode;

  @Column("RELATED_CLNT_NMBR")
  @Size(min = 8, max = 8)
  private String relatedClientNumber;

  @Column("RELATED_CLNT_LOCN")
  @Size(min = 2, max = 2)
  private String relatedClientLocationCode;

  @Column("RELATIONSHIP_CODE")
  @Size(min = 1, max = 2)
  private String relationshipType;

  @Column("SIGNING_AUTH_IND")
  @Size(max = 1, min = 1)
  private String signingAuthInd;

  @Column("PERCENT_OWNERSHIP")
  private BigDecimal percentOwnership;

  @Column("ADD_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column("ADD_USERID")
  @NotNull
  @Size(min = 1, max = 30)
  private String createdBy;

  @Column("UPDATE_TIMESTAMP")
  @NotNull
  private LocalDateTime updatedAt;

  @Column("UPDATE_USERID")
  @NotNull
  @Size(min = 1, max = 30)
  private String updatedBy;

  @Column("UPDATE_ORG_UNIT")
  @NotNull
  private Long updatedByUnit;

  @Column("ADD_ORG_UNIT")
  @NotNull
  private Long createdByUnit;

  @Column("REVISION_COUNT")
  @NotNull
  private Long revision;

  public boolean isInvalid() {
    return StringUtils.isAllBlank(
        clientNumber,
        relatedClientNumber,
        clientLocationCode,
        relatedClientLocationCode,
        relationshipType
    );
  }

  public String getId(){
    return String.format("%s%s%s%s%s",
        clientNumber,
        clientLocationCode,
        relationshipType,
        relatedClientNumber,
        relatedClientLocationCode
        );
  }
}
