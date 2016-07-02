package ft.firekiosk.birthday;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@Value("${firekiosk.abt_index}")
	private String abtIndex;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping("/api/birthdays")
	public List<Birthday> getAll() {
		final List<Birthday> allBirthdays = jdbcTemplate.query(
				"select MIG_NR, MIG_VORNAME, MIG_NACHNAME, MIG_GEB_DAT, EXTRACT(MONTH from MIG_GEB_DAT) as \"MONTH\", EXTRACT(DAY from MIG_GEB_DAT) as \"DAY\" from MIG_STAMM "
						+ "where MIG_ABT_INDEX = ? and MIG_AKTIV_BD is null order by \"MONTH\", \"DAY\"",
				new Object[] { abtIndex }, this::toBirthday);
		return allBirthdays;
	}

	public Birthday toBirthday(final ResultSet rs, final int rowNum) throws SQLException {
		final Birthday birthday = new Birthday();
		birthday.setId(rs.getString("MIG_NR"));
		birthday.setName(rs.getString("MIG_VORNAME") + " " + rs.getString("MIG_NACHNAME"));

		final LocalDate birthdate = asLocalDate(rs.getDate("MIG_GEB_DAT"));
		if (birthdate != null) {
			birthday.setDate(birthdate.toString());
			birthday.setMonth(birthdate.getMonth().getValue());
			birthday.setAge(ageOf(birthdate));
		}

		return birthday;
	}

	private static LocalDate asLocalDate(final Date date) {
		if (date == null) {
			return null;
		}
		return date.toLocalDate();
	}

	private static int ageOf(final LocalDate birthdate) {
		final LocalDate now = LocalDate.now();
		return Math.max(0, now.getYear() - birthdate.getYear());
	}
}
