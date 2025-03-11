package ca.bc.gov.app.entity;

public interface ForestClientLocationProjection {
  String getClientNumber();
  String getClientLocnCode();
  String getClientLocnName();
  String getAddressOne();
  String getAddressTwo();
  String getAddressThree();
  String getCity();
  String getProvinceCode();
  String getProvinceDesc();
  String getCountryCode();
  String getCountryDesc();
  String getPostalCode();
  String getBusinessPhone();
  String getHomePhone();
  String getCellPhone();
  String getFaxNumber();
  String getEmailAddress();
  String getLocnExpiredInd();
  String getCliLocnComment();
}