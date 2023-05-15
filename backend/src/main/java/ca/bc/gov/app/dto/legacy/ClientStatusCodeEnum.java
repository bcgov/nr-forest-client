package ca.bc.gov.app.dto.legacy;

import lombok.Getter;

@Getter
public enum ClientStatusCodeEnum {
  ACT("Active"),
  DAC("Deactivated"),
  DEC("Deceased"),
  REC("Receivership"),
  SPN("Suspended");

  private final String description;

  ClientStatusCodeEnum(String description) {
    this.description = description;
  }
}
