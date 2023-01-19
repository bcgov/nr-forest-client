package ca.bc.gov.app.entity;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@With
@Table(name = "CLIENT_STATUS_CODE", schema = "${ca.bc.gov.nrs.oracle.schema}")
public class ClientStatusCodeEntity {

  public static final String ACTIVE = "ACT"; //TODO: move to enum

  @Id
  @Column("CLIENT_STATUS_CODE")
  private String clientStatusCode;

  @Column("DESCRIPTION")
  private String description;

  @Column("EFFECTIVE_DATE")
  private LocalDate effectiveDate;

  @Column("EXPIRY_DATE")
  private LocalDate expiryDate;

  @Column("CREATE_TIMESTAMP")
  private LocalDate createTimestamp;

  @Column("UPDATE_TIMESTAMP")
  private LocalDate updateTimestamp;

  @Column("CREATE_USER")
  private String createUser;

  @Column("UPDATE_USER")
  private String updateUser;

}
