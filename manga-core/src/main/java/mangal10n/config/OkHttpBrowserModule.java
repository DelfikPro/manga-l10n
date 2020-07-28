package mangal10n.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import mangal10n.browser.Browser;
import mangal10n.browser.impl.okhttp.OkHttpBrowser;

public class OkHttpBrowserModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Browser.class).to(OkHttpBrowser.class).in(Singleton.class);
	}
}
