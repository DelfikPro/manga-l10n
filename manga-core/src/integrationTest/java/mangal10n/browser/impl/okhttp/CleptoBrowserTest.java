package mangal10n.browser.impl.okhttp;

import mangal10n.browser.Browser;
import mangal10n.browser.impl.clepto.CleptoBrowser;

public class CleptoBrowserTest extends AbstractBrowserTest {

	@Override
	protected Browser createBrowser() {
		return new CleptoBrowser();
	}
}
