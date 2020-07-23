package mangal10n.browser.impl.okhttp;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.browser.impl.BrowserException;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@AllArgsConstructor
public class OkHttpRequest implements Request {

	private final okhttp3.Request originRequest;

	@Override
	public Response execute() {
		try {
			OkHttpClient client = createClient();
			okhttp3.Response response = client.newCall(originRequest).execute();
			return new OkHttpResponse(response);
		} catch (IOException e) {
			throw new BrowserException(e);
		}
	}

	private OkHttpClient createClient() {
		return new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
	}

	@RequiredArgsConstructor
	public static class OkHttpRequestBuilder implements Request.Builder {

		private static final byte[] EMPTY_POST_DATA = new byte[0];
		private final okhttp3.Request.Builder originRequestBuilder;
		private HttpUrl.Builder okhttpUrlBuilder;
		private MultipartBody.Builder multipartBodyBuilder;
		private boolean isGetRequest = true; // false == POST
		private byte[] postData;

		@Override
		public Builder url(String url) {
			okhttpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
			return this;
		}

		@Override
		public Builder addQueryParameter(String name, String value) {
			okhttpUrlBuilder.addQueryParameter(name, value);
			return this;
		}

		@Override
		public Builder addHeader(String name, String value) {
			originRequestBuilder.addHeader(name, value);
			return this;
		}

		@Override
		public Builder addMultipartData(String key, String value) {
			prepareMultipart();

			multipartBodyBuilder.addFormDataPart(key, value);
			return this;
		}

		@Override
		public Builder addMultipartData(String name, String filename, String contentType, byte[] rawContent) {
			prepareMultipart();

			multipartBodyBuilder.addFormDataPart(name, filename, RequestBody.create(rawContent, MediaType.get(contentType)));
			return this;
		}

		@Override
		public Builder basicAuth(String user, String password) {
			addHeader("Authorization", Credentials.basic(user, password));
			return this;
		}

		@Override
		public Builder get() {
			isGetRequest = true;
			return this;
		}

		@Override
		public Builder post() {
			return post(EMPTY_POST_DATA);
		}

		@Override
		public Builder post(String string) {
			return post(string.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public Builder post(byte[] bytes) {
			isGetRequest = false;
			postData = bytes;
			return this;
		}

		@Override
		public Request build() {
			originRequestBuilder.url(okhttpUrlBuilder.build());

			if (multipartBodyBuilder != null) {
				MultipartBody multipartBody = multipartBodyBuilder.build();
				originRequestBuilder.post(multipartBody);
			} else if (!isGetRequest) {
				originRequestBuilder.post(RequestBody.create(postData));
			}

			return new OkHttpRequest(originRequestBuilder.build());
		}

		private void prepareMultipart() {
			if (multipartBodyBuilder == null) {
				multipartBodyBuilder = new MultipartBody.Builder();
				multipartBodyBuilder.setType(MultipartBody.FORM);
			}
		}
	}
}
