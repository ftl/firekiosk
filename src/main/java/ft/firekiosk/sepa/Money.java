package ft.firekiosk.sepa;

public class Money {

	public static final Money ZERO = new Money(0);

	public final int amount;

	public Money(final int amount) {
		this.amount = amount;
	}

	public static Money add(final Money m1, final Money m2) {
		return new Money(m1.amount + m2.amount);
	}

	@Override
	public String toString() {
		final String zeroPadded = String.format("%03d", amount);
		final int decimalSeparatorIndex = zeroPadded.length() - 2;
		final String formatted = zeroPadded.substring(0, decimalSeparatorIndex) + "."
				+ zeroPadded.substring(decimalSeparatorIndex);
		return formatted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Money other = (Money) obj;
		if (amount != other.amount) {
			return false;
		}
		return true;
	}
}
