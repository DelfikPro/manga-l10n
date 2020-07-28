package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import com.google.inject.*;
import mangal10n.config.OcrModule;
import mangal10n.config.OkHttpBrowserModule;
import mangal10n.textrecognition.AbstractOcrTest;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OCRWebServiceTest extends AbstractOcrTest {

	@BeforeClass
	public static void beforeClass() {
		injector = Guice.createInjector(
				new OkHttpBrowserModule(),
				new TestingModule(),
				new OcrModule()
		);
	}

	@Before
	public void before() {
		ocr = getInstanceByName(OCRWebService.class.getSimpleName());
	}

	@Test
	public void testSimpleText() throws IOException {
		final String expectedValue = IOUtils.resourceToString("/simple_text_v2.txt", StandardCharsets.UTF_8)
				.replaceAll("\r\n", "\n");
		final byte[] imageBytes = IOUtils.resourceToByteArray("/simple_text.jpg");

		String recognition = ocr.doRecognition(imageBytes);
		assertNotNull(recognition);
		assertEquals(expectedValue, recognition.replaceAll("\r\n", "\n"));
	}

	private static class TestingModule extends AbstractModule {

		@Override
		protected void configure() {
			TypeLiteral<List<WebServerUser>> typeLiteral = new TypeLiteral<>(){};
			bind(typeLiteral).toProvider(WebServerUserListProvider.class).in(Singleton.class);
			bind(Gson.class).toInstance(new Gson());
		}
	}

	private static class WebServerUserListProvider implements Provider<List<WebServerUser>> {

		@Override
		public List<WebServerUser> get() {
			final String ocrLogin = System.getProperty("ocrLogin");
			final String ocrToken = System.getProperty("ocrToken");
			System.out.printf("Login: %s | Token: %s", ocrLogin, ocrToken);

			List<WebServerUser> users;
			if (isBlank(ocrLogin) || isBlank(ocrToken)) {
				users = Collections.emptyList();
			} else {
				users = Collections.singletonList(new WebServerUser(ocrLogin, ocrToken));
			}

			return users;
		}

		private boolean isBlank(String string) {
			return string == null || string.equals("");
		}
	}
}