package ft.firekiosk.sepa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MoneyTest {
	@Test
	public void shouldFormatAmountWithTwoDecimals() throws Exception {
		assertThat(new Money(1).toString(), is(equalTo("0.01")));
		assertThat(new Money(12).toString(), is(equalTo("0.12")));
		assertThat(new Money(123).toString(), is(equalTo("1.23")));
		assertThat(new Money(1234).toString(), is(equalTo("12.34")));
		assertThat(new Money(12345).toString(), is(equalTo("123.45")));
	}
}
