package ft.firekiosk.sepa;

public class DirectDebit {

	public final Account debtor;
	public final Mandate mandate;
	public final Money amount;
	public final String text;

	public DirectDebit(final Account debtor, final Mandate mandate, final Money amount, final String text) {
		this.debtor = debtor;
		this.mandate = mandate;
		this.amount = amount;
		this.text = text;
	}
}
