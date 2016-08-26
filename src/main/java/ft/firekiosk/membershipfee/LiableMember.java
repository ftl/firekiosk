package ft.firekiosk.membershipfee;

import java.time.LocalDate;

public class LiableMember {

	private String id;
	private String name;
	private int age;
	private String accountName;
	private String iban;
	private String bic;
	private String sepaMandateId;
	private LocalDate sepaMandateDate;
	private int membershipFee;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(final int age) {
		this.age = age;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(final String accountName) {
		this.accountName = accountName;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(final String iban) {
		this.iban = iban;
	}

	public String getBic() {
		return bic;
	}

	public void setBic(final String bic) {
		this.bic = bic;
	}

	public String getSepaMandateId() {
		return sepaMandateId;
	}

	public void setSepaMandateId(final String sepaMandateId) {
		this.sepaMandateId = sepaMandateId;
	}

	public LocalDate getSepaMandateDate() {
		return sepaMandateDate;
	}

	public void setSepaMandateDate(final LocalDate sepaMandateDate) {
		this.sepaMandateDate = sepaMandateDate;
	}

	public int getMembershipFee() {
		return membershipFee;
	}

	public void setMembershipFee(final int membershipFee) {
		this.membershipFee = membershipFee;
	}
}
