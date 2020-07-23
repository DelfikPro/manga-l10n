package mangal10n.browser.impl.okhttp;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import mangal10n.browser.Response;
import mangal10n.browser.impl.BrowserException;

import java.io.IOException;

@RequiredArgsConstructor
public class OkHttpResponse implements Response {

	private final okhttp3.Response originResponse;

	@Override
	public int code() {
		return originResponse.code();
	}

	@Override
	public Body body() {
		return new OkHttpResponseBody(originResponse.body());
	}

	@Override
	public void close() {
		originResponse.close();
	}

	@AllArgsConstructor
	public static class OkHttpResponseBody implements Response.Body {

		private final okhttp3.ResponseBody originResponseBody;

		@Override
		public String string() {
			try {
				return originResponseBody.string();
			} catch (IOException e) {
				throw new BrowserException(e);
			}
		}

		@Override
		public byte[] bytes() {
			try {
				return originResponseBody.bytes();
			} catch (IOException e) {
				throw new BrowserException(e);
			}
		}
	}
}
