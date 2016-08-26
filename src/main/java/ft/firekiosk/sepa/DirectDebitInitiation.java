package ft.firekiosk.sepa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DirectDebitInitiation {

	public final Account creditorAccount;
	public final String creditorId;
	public final LocalDate collectionDate;
	public final List<DirectDebit> debits = new ArrayList<>();

	public DirectDebitInitiation(final Account creditorAccount, final String creditorId,
			final LocalDate collectionDate) {
		this.creditorAccount = creditorAccount;
		this.creditorId = creditorId;
		this.collectionDate = collectionDate;
	}

	public void add(final DirectDebit debit) {
		debits.add(debit);
	}

	public int getTransactionCount() {
		return debits.size();
	}

	public Money getTotalAmount() {
		return debits.stream().map(d -> d.amount).reduce(Money.ZERO, Money::add);
	}
}
