package ft.firekiosk.sepa;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

public class PainXmlWriterTest {

	@Test
	public void writeToConsole() throws Exception {
		final DirectDebitInitiation sdd = new DirectDebitInitiation(
				new Account("Creditor Name", "Creditor IBAN", "Creditor BIC"), "Creditor ID", LocalDate.of(2016, 9, 1));

		sdd.add(new DirectDebit(new Account("Account Name One", "IBAN 1", "BIC 1"),
				new Mandate("Mandate ID 1", LocalDate.of(2015, 12, 11)), new Money(100), "Testdebit number one"));
		sdd.add(new DirectDebit(new Account("Account Name Two", "IBAN 2", "BIC 2"),
				new Mandate("Mandate ID 2", LocalDate.of(2015, 1, 2)), new Money(200), "Testdebit number two"));

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PainXmlWriter.write(sdd, buffer);

		prettyPrint(buffer.toString());
	}

	public void prettyPrint(final String xml) throws XMLStreamException, TransformerException {
		final Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		final Writer out = new StringWriter();
		t.transform(new StreamSource(new StringReader(xml)), new StreamResult(out));

		System.out.println(out);
	}
}
