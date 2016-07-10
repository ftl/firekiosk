package ft.firekiosk.addressbook;

public class ContactNumber {
	private ContactDevice device;
	private ContactType type;
	private String number;

	public ContactDevice getDevice() {
		return device;
	}

	public void setDevice(final ContactDevice device) {
		this.device = device;
	}

	public ContactType getType() {
		return type;
	}

	public void setType(final ContactType type) {
		this.type = type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(final String number) {
		this.number = number;
	}
}
