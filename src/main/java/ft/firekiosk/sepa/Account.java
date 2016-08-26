package ft.firekiosk.sepa;

public class Account {

	public final String name;
	public final String iban;
	public final String bic;

	public Account(final String name, final String iban, final String bic) {
		this.name = name;
		this.iban = iban;
		this.bic = bic;
	}
}
