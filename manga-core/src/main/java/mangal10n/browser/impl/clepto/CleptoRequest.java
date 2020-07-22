package mangal10n.browser.impl.clepto;

import clepto.net.Method;
import lombok.AllArgsConstructor;
import mangal10n.browser.Request;
import mangal10n.browser.Response;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

import static java.net.Proxy.NO_PROXY;

@AllArgsConstructor
public class CleptoRequest implements Request {

	private final clepto.net.Request originRequest;

	@Override
	public Response execute() {
		return new CleptoResponse(originRequest.execute(NO_PROXY));
	}

	public static class CleptoRequestBuilder implements Request.Builder {

		private final Map<String, String> headers = new HashMap<>();
		private final Map<String, String> urlParams = new HashMap<>();
		private String url;
		private MultiPartBodyPublisher multipartBody;

		@Override
		public Builder url(String url) {
			this.url = url;
			return this;
		}

		@Override
		public Builder addQueryParameter(String name, String value) {
			urlParams.put(name, value);
			return this;
		}

		@Override
		public Builder addHeader(String name, String value) {
			headers.put(name, value);
			return this;
		}

		@Override
		public Builder addMultipartData(String name, String value) {
			prepareMultipart();

			multipartBody.addPart(name, value);
			return this;
		}

		@Override
		public Builder addMultipartData(String name, String filename, String contentType, byte[] rawContent) {
			prepareMultipart();

			multipartBody.addPart(name, () -> new ByteArrayInputStream(rawContent), filename, contentType);
			return this;
		}

		@Override
		public Request build() {
			if (multipartBody != null) {
				HttpRequest.Builder builder = HttpRequest.newBuilder()
						.uri(URI.create(url));
				headers.forEach(builder::header);
				builder.POST(multipartBody.buildForJavaNet());
				return new JavaNetRequest(builder);
			} else {
				clepto.net.Request request = new clepto.net.Request(url, Method.GET);
				urlParams.forEach(request::param);

				return new CleptoRequest(request);
			}
		}

		private void prepareMultipart() {
			if (multipartBody == null) {
				multipartBody = new MultiPartBodyPublisher();
				addHeader("Content-Type", "multipart/form-data; boundary=" + multipartBody.getBoundary());
			}
		}
	}
}
