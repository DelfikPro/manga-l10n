package clepto.net;

import lombok.Data;

@Data
public class CookieCutter {

	private final String name;
	private final String description;

	public Cookie cut(String value) {
		return new BakedCookie(this, value);
	}

}
