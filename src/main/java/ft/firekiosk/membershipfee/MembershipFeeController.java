package ft.firekiosk.membershipfee;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ft.firekiosk.sepa.Account;
import ft.firekiosk.sepa.DirectDebit;
import ft.firekiosk.sepa.DirectDebitInitiation;
import ft.firekiosk.sepa.Mandate;
import ft.firekiosk.sepa.Money;
import ft.firekiosk.sepa.PainXmlWriter;

@RestController
public class MembershipFeeController {

	@Value("${firekiosk.abt_index}")
	private String abtIndex;
	@Value("${firekiosk.sepa.creditor.name}")
	private String creditorName;
	@Value("${firekiosk.sepa.creditor.id}")
	private String creditorId;
	@Value("${firekiosk.sepa.creditor.iban}")
	private String creditorIban;
	@Value("${firekiosk.sepa.creditor.bic}")
	private String creditorBic;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping("/api/liableMembers")
	public List<LiableMember> getLiableMembers(
			@RequestParam(value = "collectionDate", required = true) final String collectionDateParam) {
		final LocalDate collectionDate = parseCollectionDate(collectionDateParam);
		return getLiableMembers(collectionDate);
	}

	private static LocalDate parseCollectionDate(final String collectionDate) {
		try {
			return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(collectionDate));
		} catch (final DateTimeException e) {
			throw new InvalidCollectionDate();
		}
	}

	private List<LiableMember> getLiableMembers(final LocalDate collectionDate) {
		final List<LiableMember> liableMembers = jdbcTemplate.query(
				"select MIG_ID, MIG_VORNAME, MIG_NACHNAME, MIG_GEB_DAT, MIG_KONTOINHABER, MIG_MANDAT_REF, MIG_MANDAT_VD, MIG_IBAN, MIG_BIC, MIG_BEITRAG from MIG_STAMM "
						+ "where MIG_ABT_INDEX = ? and MIG_AKTIV_BD is null and MIG_GEB_DAT is not null and MIG_BEITRAG_ART is not null and MIG_BEITRAG is not null "
						+ "order by MIG_NACHNAME, MIG_VORNAME, MIG_GEB_DAT",
				new Object[] { abtIndex }, (rs, rowNum) -> toLiableMember(rs, rowNum, collectionDate));
		return liableMembers;
	}

	private static LiableMember toLiableMember(final ResultSet rs, final int rowNum, final LocalDate collectionDate)
			throws SQLException {
		final LiableMember member = new LiableMember();
		member.setId(rs.getString("MIG_ID").trim());
		member.setName(String.join(" ", rs.getString("MIG_VORNAME"), rs.getString("MIG_NACHNAME")));
		member.setAge(ageOf(asLocalDate(rs.getDate("MIG_GEB_DAT")), collectionDate));
		member.setAccountName(rs.getString("MIG_KONTOINHABER").trim());
		member.setIban(rs.getString("MIG_IBAN").trim());
		member.setBic(rs.getString("MIG_BIC").trim());
		member.setSepaMandateId(rs.getString("MIG_MANDAT_REF").trim());
		member.setSepaMandateDate(asLocalDate(rs.getDate("MIG_MANDAT_VD")));
		member.setMembershipFee(asAmountOfMoney(rs.getString("MIG_BEITRAG").trim()));

		return member;
	}

	private static LocalDate asLocalDate(final Date date) {
		if (date == null) {
			return null;
		}
		return date.toLocalDate();
	}

	private static int asAmountOfMoney(final String value) {
		final String withoutSeparator = value.replace(",", "");
		try {
			return Integer.parseInt(withoutSeparator);
		} catch (final NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static int ageOf(final LocalDate birthdate, final LocalDate collectionDate) {
		return Math.max(0, collectionDate.getYear() - birthdate.getYear());
	}

	@RequestMapping(path = "/api/liableMembers/sepa", produces = "application/xml")
	public @ResponseBody ResponseEntity<byte[]> getSepaDirectDebitInitiation(
			@RequestParam(value = "collectionDate", required = true) final String collectionDateParam,
			@RequestParam(value = "debitText", required = true) final String debitTextParam) {
		final LocalDate collectionDate = parseCollectionDate(collectionDateParam);
		final String debitText = parseDebitText(debitTextParam);

		return new ResponseEntity<>(getSepaDirectDebitInitiation(collectionDate, debitText), HttpStatus.OK);
	}

	private static String parseDebitText(final String debitText) {
		if (debitText == null || debitText.isEmpty()) {
			throw new InvalidDebitText();
		}
		return debitText;
	}

	private byte[] getSepaDirectDebitInitiation(final LocalDate collectionDate, final String debitText) {
		final DirectDebitInitiation sdd = new DirectDebitInitiation(
				new Account(creditorName, creditorIban, creditorBic), creditorId, collectionDate);

		for (final LiableMember member : getLiableMembers(collectionDate)) {
			sdd.add(new DirectDebit(new Account(member.getAccountName(), member.getIban(), member.getBic()),
					new Mandate(member.getSepaMandateId(), member.getSepaMandateDate()),
					new Money(member.getMembershipFee()), debitText));
		}

		try {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			PainXmlWriter.write(sdd, buffer);
			return buffer.toByteArray();
		} catch (final XMLStreamException e) {
			e.printStackTrace();
			throw new InternalServerError();
		}
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class InvalidCollectionDate extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidCollectionDate() {
			super("The given collection date is invalid. Provide a collection date in the ISO date format (yyyy-mm-dd).");
		}
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class InvalidDebitText extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidDebitText() {
			super("The given debit text is invalid.");
		}
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public static class InternalServerError extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
