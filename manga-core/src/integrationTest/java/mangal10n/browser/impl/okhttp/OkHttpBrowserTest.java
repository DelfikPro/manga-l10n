package mangal10n.browser.impl.okhttp;

import mangal10n.browser.Browser;

public class OkHttpBrowserTest extends AbstractBrowserTest {

	@Override
	protected Browser createBrowser() {
		return new OkHttpBrowser();
	}
}
