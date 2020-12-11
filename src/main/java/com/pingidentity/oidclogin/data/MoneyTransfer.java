package com.pingidentity.oidclogin.data;

import java.util.UUID;

public class MoneyTransfer {

	private String id;
	private String beneficiaryName;
	private String beneficiaryIBAN;
	private String amount;
	private String purpose;

	public MoneyTransfer() {
		super();
		id = UUID.randomUUID().toString();
	}

	public MoneyTransfer(String beneficiaryName, String beneficiaryIBAN, String amount, String purpose) {
		super();
		id = UUID.randomUUID().toString();
		this.beneficiaryName = beneficiaryName;
		this.beneficiaryIBAN = beneficiaryIBAN;
		this.amount = amount;
		this.purpose = purpose;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getBeneficiaryIBAN() {
		return beneficiaryIBAN;
	}

	public void setBeneficiaryIBAN(String beneficiaryIBAN) {
		this.beneficiaryIBAN = beneficiaryIBAN;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

}
