package ft.firekiosk.sepa;

import java.time.LocalDate;

public class Mandate {

	public final String id;
	public final LocalDate dateOfSignature;

	public Mandate(final String id, final LocalDate dateOfSignature) {
		this.id = id;
		this.dateOfSignature = dateOfSignature;
	}
}
