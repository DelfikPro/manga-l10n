package clepto.net;

import lombok.Data;

@Data
public class BakedCookie implements clepto.net.Cookie {

	private final clepto.net.CookieCutter cutFrom;
	private final String value;

	@Override
	public String toString() {
		return getCutFrom().getName() + "=" + getValue();
	}

	public String getName() {
		return cutFrom.getName();
	}

}
