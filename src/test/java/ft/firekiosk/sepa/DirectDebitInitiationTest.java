package ft.firekiosk.sepa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DirectDebitInitiationTest {

	@Test
	public void shouldProvideSumOfAllTransations() throws Exception {
		final DirectDebitInitiation sdd = new DirectDebitInitiation(null, null, null);
		sdd.add(new DirectDebit(null, null, new Money(1), null));
		sdd.add(new DirectDebit(null, null, new Money(2), null));
		sdd.add(new DirectDebit(null, null, new Money(3), null));

		assertThat(sdd.getTotalAmount(), is(equalTo(new Money(6))));
	}
}
