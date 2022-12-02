package ca.bc.gov.app.m.oracle.legacyclient.vo;

import java.io.Serializable;

public class ClientPublicViewVO implements Serializable {

	private static final long serialVersionUID = -3558915712555555036L;

	public String clientNumber;
	public String incorporationNumber;
	public String clientName;
	public String legalFirstName;
	public String legalMiddleName;
	public String clientStatusCode;
	public String clientTypeCode;
	public String clientNameInOrgBook;
	public boolean sameName;
	
	public ClientPublicViewVO() {
		super();
	}

	public ClientPublicViewVO(String clientNumber, 
							  String clientName, 
							  String legalFirstName, 
							  String legalMiddleName,
							  String clientStatusCode, 
							  String clientTypeCode) {
		super();
		this.clientNumber = clientNumber;
		this.clientName = clientName;
		this.legalFirstName = legalFirstName;
		this.legalMiddleName = legalMiddleName;
		this.clientStatusCode = clientStatusCode;
		this.clientTypeCode = clientTypeCode;
	}

	public ClientPublicViewVO(String clientNumber, 
							  String clientName, 
							  String legalFirstName, 
							  String legalMiddleName,
							  String clientStatusCode, 
							  String clientTypeCode, 
							  String clientNameInOrgBook) {
		super();
		this.clientNumber = clientNumber;
		this.clientName = clientName;
		this.legalFirstName = legalFirstName;
		this.legalMiddleName = legalMiddleName;
		this.clientStatusCode = clientStatusCode;
		this.clientTypeCode = clientTypeCode;
		this.clientNameInOrgBook = clientNameInOrgBook;
	}

	@Override
	public String toString() {
		return "ClientPublicViewVO [clientNumber=" + clientNumber + ", clientName=" + clientName + ", legalFirstName="
				+ legalFirstName + ", legalMiddleName=" + legalMiddleName + ", clientStatusCode=" + clientStatusCode
				+ ", clientTypeCode=" + clientTypeCode + ", clientNameInOrgBook=" + clientNameInOrgBook + ", sameName="
				+ sameName + "]";
	}

}
