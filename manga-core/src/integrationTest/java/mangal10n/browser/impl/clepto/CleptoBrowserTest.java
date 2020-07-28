package mangal10n.browser.impl.clepto;

import com.google.inject.Guice;
import mangal10n.browser.impl.AbstractBrowserTest;
import mangal10n.config.CleptoBrowserModule;
import mangal10n.config.GsonModule;
import org.junit.BeforeClass;

public class CleptoBrowserTest extends AbstractBrowserTest {

	@BeforeClass
	public static void beforeClass() {
		injector = Guice.createInjector(
				new GsonModule(),
				new CleptoBrowserModule()
		);
	}
}
