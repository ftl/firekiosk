package ft.firekiosk.birthday;

public class Birthday {

	private String id;
	private String name;
	private String date;
	private int month;
	private int age;

	public Birthday() {
	}

	public Birthday(final String id, final String name, final String date, final int month, final int age) {
		this.id = id;
		this.name = name;
		this.date = date;
		this.month = month;
		this.age = age;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(final int month) {
		this.month = month;
	}

	public int getAge() {
		return age;
	}

	public void setAge(final int age) {
		this.age = age;
	}
}
