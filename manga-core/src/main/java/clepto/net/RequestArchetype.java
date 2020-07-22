package clepto.net;

import lombok.Getter;

import java.net.Proxy;
import java.util.Arrays;
import java.util.Collection;

public abstract class RequestArchetype {

	@Getter
	private final Collection<Cookie> cookies;

	protected RequestArchetype(Cookie... cookies) {
		this(Arrays.asList(cookies));
	}

	protected RequestArchetype(Collection<Cookie> cookies) {
		this.cookies = cookies;
	}

	protected abstract Request execute(String... args);

	public Response accept(Proxy proxy, String... args) {
		return execute(args).execute(proxy).args(args);
	}

}
