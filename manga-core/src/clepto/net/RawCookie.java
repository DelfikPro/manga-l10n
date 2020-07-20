package clepto.net;

import lombok.Data;

@Data
public class RawCookie implements Cookie {
	private final String name;
	private final String value;

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}
}
