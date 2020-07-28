package mangal10n.browser.impl.okhttp;

import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import okhttp3.OkHttpClient;

import java.time.Duration;

public class OkHttpBrowser implements Browser {

	private final OkHttpClient client;

	public OkHttpBrowser() {
		client = new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
	}

	@Override
	public Request.Builder requestBuilder() {
		return new OkHttpRequest.OkHttpRequestBuilder(client, new okhttp3.Request.Builder());
	}
}
