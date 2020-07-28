package mangal10n.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import mangal10n.browser.Browser;
import mangal10n.browser.impl.clepto.CleptoBrowser;

public class CleptoBrowserModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Browser.class).to(CleptoBrowser.class).in(Singleton.class);
	}
}
