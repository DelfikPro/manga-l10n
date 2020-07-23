package mangal10n.browser.impl.okhttp;

import mangal10n.browser.Browser;
import mangal10n.browser.Request;

public class OkHttpBrowser implements Browser {

	@Override
	public Request.Builder requestBuilder() {
		return new OkHttpRequest.OkHttpRequestBuilder(new okhttp3.Request.Builder());
	}
}
