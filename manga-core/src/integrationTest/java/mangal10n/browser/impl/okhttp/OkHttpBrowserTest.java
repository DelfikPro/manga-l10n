package mangal10n.browser.impl.okhttp;

import com.google.inject.Guice;
import mangal10n.browser.impl.AbstractBrowserTest;
import mangal10n.config.GsonModule;
import mangal10n.config.OkHttpBrowserModule;
import org.junit.BeforeClass;

public class OkHttpBrowserTest extends AbstractBrowserTest {

	@BeforeClass
	public static void beforeClass() {
		injector = Guice.createInjector(
				new GsonModule(),
				new OkHttpBrowserModule()
		);
	}
}
