package ca.bc.gov.app.entity.client;

import ca.bc.gov.app.ApplicationConstant;
import io.r2dbc.postgresql.codec.Json;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "email_log", schema = ApplicationConstant.POSTGRES_ATTRIBUTE_SCHEMA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class EmailLogEntity {

  @Id
  @Column("email_log_id")
  private Integer emailLogId;
  
  @Column("email_id")
  @Size(min = 1, max = 40)
  private String emailId;
  
  @Column("email_sent_ind")
  @Size(min = 1, max = 1)
  private String emailSentInd;
  
  @Column("exception_message")
  private String exceptionMessage;
  
  @Column("template_name")
  @NotNull
  @Size(min = 3, max = 40)
  private String templateName;
  
  @Column("email_address")
  @NotNull
  @Size(min = 5, max = 100)
  private String emailAddress;
  
  @Column("email_subject")
  @NotNull
  @Size(min = 5, max = 100)
  private String emailSubject;
  
  @Column("email_variables")
  private Json emailVariables;
  
  @Column("create_timestamp")
  @NotNull
  private LocalDateTime createDate;
  
  @Column("update_timestamp")
  private LocalDateTime updateDate;

  @Transient
  private Map<String,Object> variables;
}
