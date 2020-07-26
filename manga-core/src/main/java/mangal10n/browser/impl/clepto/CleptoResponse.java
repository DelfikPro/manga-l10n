package mangal10n.browser.impl.clepto;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import mangal10n.browser.Response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class CleptoResponse implements Response {

	private final clepto.net.Response originResponse;

	@Override
	public int code() {
		return originResponse.getCode();
	}

	@Override
	public String header(String name) {
		return originResponse.getHeader(name);
	}

	@Override
	public Body body() {
		return new CleptoResponseBody(originResponse.getBody());
	}

	@Override
	public void close() {
		//nothing...
	}

	@AllArgsConstructor
	public static class CleptoResponseBody implements Body {

		private final byte[] body;

		@Override
		public String string() {
			return new String(body, StandardCharsets.UTF_8);
		}

		@Override
		public byte[] bytes() {
			return body;
		}

		@Override
		public InputStream byteStream() {
			return new ByteArrayInputStream(body);
		}
	}
}
