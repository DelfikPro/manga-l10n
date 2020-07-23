package mangal10n.browser.impl.clepto;

import lombok.RequiredArgsConstructor;
import mangal10n.browser.Response;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class JavaNetResponse implements Response {

	private final HttpResponse<String> originResponse;

	@Override
	public int code() {
		return originResponse.statusCode();
	}

	@Override
	public Body body() {
		return new JavaNetResponseBody(originResponse);
	}

	@Override
	public void close() {
		//nothing...
	}

	@RequiredArgsConstructor
	public static class JavaNetResponseBody implements Body {

		private final HttpResponse<String> originResponse;

		@Override
		public String string() {
			return originResponse.body();
		}

		@Override
		public byte[] bytes() {
			return originResponse.body().getBytes(StandardCharsets.UTF_8);
		}
	}
}
