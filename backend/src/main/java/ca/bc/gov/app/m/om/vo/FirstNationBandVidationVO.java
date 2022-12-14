package ca.bc.gov.app.m.om.vo;

import java.io.Serializable;

public class FirstNationBandVidationVO implements Serializable {

  private static final long serialVersionUID = 3621133933076995146L;

  public String clientNumber;

  public String corpRegnNmbr;

  public String clientName;

  public String sourceClientName;

  public Boolean nameMatch;

  public String addressOne;

  public String sourceAddressOne;

  public String addressTwo;

  public String sourceAddressTwo;

  public String city;

  public String sourceCity;

  public String province;

  public String sourceProvince;

  public String postalCode;

  public String sourcePostalCode;

  public Boolean addressMatch;

  public FirstNationBandVidationVO() {
    super();
  }

  public FirstNationBandVidationVO(String clientNumber, String corpRegnNmbr, String clientName,
                                   String sourceClientName, String addressOne,
                                   String sourceAddressOne, String addressTwo,
                                   String sourceAddressTwo, String city, String sourceCity,
                                   String province, String sourceProvince,
                                   String postalCode, String sourcePostalCode) {
    super();
    this.clientNumber = clientNumber;
    this.corpRegnNmbr = corpRegnNmbr;
    this.clientName = clientName;
    this.sourceClientName = sourceClientName;
    this.nameMatch = clientName.equalsIgnoreCase(sourceClientName);
    this.addressOne = addressOne;
    this.sourceAddressOne = sourceAddressOne;
    this.addressTwo = addressTwo;
    this.sourceAddressTwo = sourceAddressTwo;
    this.city = city;
    this.sourceCity = sourceCity;
    this.province = province;
    this.sourceProvince = sourceProvince;
    this.postalCode = postalCode;
    this.sourcePostalCode = sourcePostalCode;
    this.addressMatch = ((addressOne != null && sourceAddressOne != null &&
        addressOne.equalsIgnoreCase(sourceAddressOne.replace(".", "")))
        || (addressTwo != null && sourceAddressTwo != null &&
        addressTwo.equalsIgnoreCase(sourceAddressTwo.replace(".", ""))))
        && (city != null && city.equalsIgnoreCase(sourceCity))
        &&
        (postalCode != null && postalCode.equalsIgnoreCase(sourcePostalCode.replaceAll("\\s", "")));
  }

  @Override
  public String toString() {
    return "FirstNationBandVidationVO [clientNumber=" + clientNumber + ", corpRegnNmbr=" +
        corpRegnNmbr
        + ", clientName=" + clientName + ", sourceClientName=" + sourceClientName + ", nameMatch=" +
        nameMatch
        + ", addressOne=" + addressOne + ", sourceAddressOne=" + sourceAddressOne +
        ", addressTwo=" + addressTwo
        + ", sourceAddressTwo=" + sourceAddressTwo + ", city=" + city + ", sourceCity=" + sourceCity
        + ", province=" + province + ", sourceProvince=" + sourceProvince + ", postalCode=" +
        postalCode
        + ", sourcePostalCode=" + sourcePostalCode + ", addressMatch=" + addressMatch + "]";
  }

}
