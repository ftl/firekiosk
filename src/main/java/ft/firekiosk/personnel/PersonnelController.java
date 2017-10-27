package ft.firekiosk.personnel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonnelController {

	@Value("${firekiosk.abt_index}")
	private String abtIndex;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping(path = "/api/personnel/elion", produces = "text/plain")
	public @ResponseBody ResponseEntity<String> getPersonnelForELion() {
		final List<Person> personnel = jdbcTemplate.query(
				"select PER_VORNAME, PER_NACHNAME from PER_STAMM where PER_ABT_INDEX = ? and PER_AKTIV_BD is null order by PER_NACHNAME, PER_VORNAME",
				new Object[] { abtIndex }, PersonnelController::toPerson);
		final List<String> personnelNames = personnel.stream().map(p -> p.getLastname() + ", " + p.getFirstname())
				.collect(Collectors.toList());
		final String result = String.join("\n", personnelNames);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private static Person toPerson(final ResultSet rs, final int rowNum) throws SQLException {
		final Person person = new Person();
		person.setFirstname(toString(rs, "PER_VORNAME", "").trim());
		person.setLastname(toString(rs, "PER_NACHNAME", "").trim());
		return person;
	}

	private static String toString(final ResultSet rs, final String fieldName, final String defaultValue)
			throws SQLException {
		final String value = rs.getString(fieldName);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
}
