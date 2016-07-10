package ft.firekiosk.addressbook;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddressbookController {

	@Value("${firekiosk.abt_index}")
	private String abtIndex;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping("/api/addresses")
	public List<Address> getAll() {
		final List<Address> allAddresses = jdbcTemplate.query(
				"select MIG_ID, MIG_VORNAME, MIG_NACHNAME, MIG_STRASSE, MIG_ORTSTEIL, MIG_PLZ, MIG_ORT, MIG_TEL_PRIV, MIG_TEL_GES, MIG_MOB_PRIV, MIG_MOB_GES, MIG_FAX_PRIV, MIG_FAX_GES, MIG_MAIL_PRIV, MIG_MAIL_GES from MIG_STAMM "
						+ "where MIG_ABT_INDEX = ? and MIG_AKTIV_BD is null order by MIG_NACHNAME, MIG_VORNAME",
				new Object[] { abtIndex }, this::toAddress);
		return allAddresses;
	}

	public Address toAddress(final ResultSet rs, final int rowNum) throws SQLException {
		final Address address = new Address();
		address.setId(rs.getString("MIG_ID").trim());
		address.setFirstname(rs.getString("MIG_VORNAME"));
		address.setLastname(rs.getString("MIG_NACHNAME"));
		address.setStreet(rs.getString("MIG_STRASSE"));
		address.setZip(rs.getString("MIG_PLZ"));
		address.setTown(toTown(rs.getString("MIG_ORT"), rs.getString("MIG_ORTSTEIL")));

		final List<ContactNumber> numbers = new ArrayList<>();
		addNumber(numbers, ContactDevice.PHONE, ContactType.PRIVATE, rs.getString("MIG_TEL_PRIV"));
		addNumber(numbers, ContactDevice.PHONE, ContactType.BUSINESS, rs.getString("MIG_TEL_GES"));
		addNumber(numbers, ContactDevice.FAX, ContactType.PRIVATE, rs.getString("MIG_FAX_PRIV"));
		addNumber(numbers, ContactDevice.FAX, ContactType.BUSINESS, rs.getString("MIG_FAX_GES"));
		addNumber(numbers, ContactDevice.MOBILE, ContactType.PRIVATE, rs.getString("MIG_MOB_PRIV"));
		addNumber(numbers, ContactDevice.MOBILE, ContactType.BUSINESS, rs.getString("MIG_MOB_GES"));
		address.setNumbers(numbers.toArray(new ContactNumber[numbers.size()]));

		final List<ContactNumber> emails = new ArrayList<>();
		addNumber(emails, ContactDevice.EMAIL, ContactType.PRIVATE, rs.getString("MIG_MAIL_PRIV"));
		addNumber(emails, ContactDevice.EMAIL, ContactType.BUSINESS, rs.getString("MIG_MAIL_GES"));
		address.setEmails(emails.toArray(new ContactNumber[emails.size()]));

		return address;
	}

	private static String toTown(final String... elements) {
		final StringJoiner joiner = new StringJoiner(" - ");
		for (final CharSequence element : elements) {
			if (element != null) {
				joiner.add(element);
			}
		}
		return joiner.toString();
	}

	private static void addNumber(final List<ContactNumber> numbers, final ContactDevice device, final ContactType type,
			final String number) {
		if (number == null || number.isEmpty()) {
			return;
		}

		final ContactNumber contactNumber = new ContactNumber();
		contactNumber.setDevice(device);
		contactNumber.setType(type);
		contactNumber.setNumber(number);
		numbers.add(contactNumber);
	}
}
