package ca.bc.gov.app.models.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubmissionStatusEnum {
  P("In Progress"),
  A("Approved"),
  R("Rejected"),
  D("Deleted"),
  S("Submitted");

  private final String description;
}
