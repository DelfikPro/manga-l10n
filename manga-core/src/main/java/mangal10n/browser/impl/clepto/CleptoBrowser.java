package mangal10n.browser.impl.clepto;

import mangal10n.browser.Browser;
import mangal10n.browser.Request;

public class CleptoBrowser implements Browser {

	@Override
	public Request.Builder requestBuilder() {
		return new CleptoRequest.CleptoRequestBuilder();
	}
}
