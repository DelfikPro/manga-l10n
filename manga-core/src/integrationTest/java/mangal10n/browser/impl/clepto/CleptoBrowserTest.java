package mangal10n.browser.impl.clepto;

import mangal10n.browser.Browser;
import mangal10n.browser.impl.AbstractBrowserTest;

public class CleptoBrowserTest extends AbstractBrowserTest {

	@Override
	protected Browser createBrowser() {
		return new CleptoBrowser();
	}
}
