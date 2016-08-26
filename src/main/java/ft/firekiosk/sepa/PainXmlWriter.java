package ft.firekiosk.sepa;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class PainXmlWriter {

	private static final DateTimeFormatter MESSAGE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");

	private final XMLStreamWriter xml;
	private final TransactionIdGenerator transactionIds = new TransactionIdGenerator();

	private PainXmlWriter(final XMLStreamWriter xml) {
		this.xml = xml;
	}

	public static void write(final DirectDebitInitiation sdd, final OutputStream out) throws XMLStreamException {
		final XMLOutputFactory output = XMLOutputFactory.newInstance();
		final XMLStreamWriter xml = output.createXMLStreamWriter(out, "UTF-8");

		final PainXmlWriter pain = new PainXmlWriter(xml);
		pain.write(sdd);
	}

	private void write(final DirectDebitInitiation sdd) throws XMLStreamException {
		startDocument();
		writeDirectDebitInitiation(sdd);
		endDocument();

		xml.flush();
	}

	private void startDocument() throws XMLStreamException {
		xml.writeStartDocument("UTF-8", "1.0");
		xml.writeStartElement("Document");
		xml.writeDefaultNamespace("urn:iso:std:iso:20022:tech:xsd:pain.008.002.02");
		xml.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xml.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
				"urn:iso:std:iso:20022:tech:xsd:pain.008.002.02 pain.008.002.02.xsd");
	}

	private void endDocument() throws XMLStreamException {
		xml.writeEndElement();
		xml.writeEndDocument();
	}

	private void writeDirectDebitInitiation(final DirectDebitInitiation sdd) throws XMLStreamException {
		xml.writeStartElement("CstmrDrctDbtInitn");

		final LocalDateTime creationDateTime = LocalDateTime.now();
		xml.writeStartElement("GrpHdr");

		writeDataValue("MsgId", generateMessageId(creationDateTime));
		writeDataValue("CreDtTm", creationDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
		writeDataValue("NbOfTxs", Integer.toString(sdd.getTransactionCount()));
		xml.writeStartElement("InitgPty");
		writeDataValue("Nm", sdd.creditorAccount.name);
		xml.writeEndElement();

		xml.writeEndElement();

		xml.writeStartElement("PmtInf");

		writeDataValue("PmtInfId", "PMT-ID-01");
		writeDataValue("PmtMtd", "DD");
		writeDataValue("BtchBookg", "true");
		writeDataValue("NbOfTxs", Integer.toString(sdd.getTransactionCount()));
		writeDataValue("CtrlSum", sdd.getTotalAmount().toString());
		xml.writeStartElement("PmtTpInf");
		xml.writeStartElement("SvcLvl");
		writeDataValue("Cd", "SEPA");
		xml.writeEndElement();
		xml.writeStartElement("LclInstrm");
		writeDataValue("Cd", "CORE");
		xml.writeEndElement();
		writeDataValue("SeqTp", "FRST");
		xml.writeEndElement();

		writeDataValue("ReqdColltnDt", sdd.collectionDate.toString());

		xml.writeStartElement("Cdtr");
		writeDataValue("Nm", sdd.creditorAccount.name);
		xml.writeEndElement();

		xml.writeStartElement("CdtrAcct");
		xml.writeStartElement("Id");
		writeDataValue("IBAN", sdd.creditorAccount.iban);
		xml.writeEndElement();
		xml.writeEndElement();

		xml.writeStartElement("CdtrAgt");
		xml.writeStartElement("FinInstnId");
		writeDataValue("BIC", sdd.creditorAccount.bic);
		xml.writeEndElement();
		xml.writeEndElement();

		writeDataValue("ChrgBr", "SLEV");

		xml.writeStartElement("CdtrSchmeId");
		xml.writeStartElement("Id");
		xml.writeStartElement("PrvtId");
		xml.writeStartElement("Othr");
		writeDataValue("Id", sdd.creditorId);
		xml.writeStartElement("SchmeNm");
		writeDataValue("Prtry", "SEPA");
		xml.writeEndElement();
		xml.writeEndElement();
		xml.writeEndElement();
		xml.writeEndElement();
		xml.writeEndElement();

		writeDebitTransactions(sdd);

		xml.writeEndElement();

		xml.writeEndElement();
	}

	private void writeDebitTransactions(final DirectDebitInitiation sdd) throws XMLStreamException {
		for (final DirectDebit debit : sdd.debits) {
			writeDebitTransaction(debit);
		}
	}

	private void writeDebitTransaction(final DirectDebit debit) throws XMLStreamException {
		xml.writeStartElement("DrctDbtTxInf");

		xml.writeStartElement("PmtId");
		writeDataValue("EndToEndId", transactionIds.next());
		xml.writeEndElement();

		xml.writeStartElement("InstdAmt");
		xml.writeAttribute("Ccy", "EUR");
		xml.writeCharacters(debit.amount.toString());
		xml.writeEndElement();

		xml.writeStartElement("DrctDbtTx");
		xml.writeStartElement("MndtRltdInf");
		writeDataValue("MndtId", debit.mandate.id);
		writeDataValue("DtOfSgntr", debit.mandate.dateOfSignature.toString());
		writeDataValue("AmdmntInd", "false");
		xml.writeEndElement();
		xml.writeEndElement();

		xml.writeStartElement("DbtrAgt");
		xml.writeStartElement("FinInstnId");
		writeDataValue("BIC", debit.debtor.bic);
		xml.writeEndElement();
		xml.writeEndElement();

		xml.writeStartElement("Dbtr");
		writeDataValue("Nm", debit.debtor.name);
		xml.writeEndElement();

		xml.writeStartElement("DbtrAcct");
		xml.writeStartElement("Id");
		writeDataValue("IBAN", debit.debtor.iban);
		xml.writeEndElement();
		xml.writeEndElement();

		xml.writeStartElement("RmtInf");
		writeDataValue("Ustrd", debit.text);
		xml.writeEndElement();

		xml.writeEndElement();
	}

	private void writeDataValue(final String elementName, final String value) throws XMLStreamException {
		xml.writeStartElement(elementName);
		xml.writeCharacters(value);
		xml.writeEndElement();
	}

	private String generateMessageId(final LocalDateTime creationDateTime) {
		return "FFB0001-" + creationDateTime.format(MESSAGE_ID_FORMAT);
	}

	private static class TransactionIdGenerator {

		private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
		private final String prefix;
		private int currentId = 0;

		public TransactionIdGenerator() {
			prefix = "REF" + LocalDate.now().format(DATE_FORMAT) + "-";
		}

		public String next() {
			final String id = prefix + String.format("%04d", currentId);
			currentId += 1;
			return id;
		}
	}
}
