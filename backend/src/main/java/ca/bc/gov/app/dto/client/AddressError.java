package ca.bc.gov.app.dto.client;

public interface AddressError {
  String error();
  String description();
  String cause();
  String resolution();
}
