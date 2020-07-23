package mangal10n.browser.impl.okhttp;

import mangal10n.browser.Browser;
import mangal10n.browser.impl.AbstractBrowserTest;

public class OkHttpBrowserTest extends AbstractBrowserTest {

	@Override
	protected Browser createBrowser() {
		return new OkHttpBrowser();
	}
}
