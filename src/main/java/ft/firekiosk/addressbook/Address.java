package ft.firekiosk.addressbook;

public class Address {

	private String id;
	private String firstname;
	private String lastname;
	private String street;
	private String zip;
	private String town;
	private ContactNumber[] numbers;
	private ContactNumber[] emails;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(final String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(final String lastname) {
		this.lastname = lastname;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(final String street) {
		this.street = street;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(final String zip) {
		this.zip = zip;
	}

	public String getTown() {
		return town;
	}

	public void setTown(final String town) {
		this.town = town;
	}

	public ContactNumber[] getNumbers() {
		return numbers;
	}

	public void setNumbers(final ContactNumber... numbers) {
		this.numbers = numbers;
	}

	public ContactNumber[] getEmails() {
		return emails;
	}

	public void setEmails(final ContactNumber[] emails) {
		this.emails = emails;
	}
}
