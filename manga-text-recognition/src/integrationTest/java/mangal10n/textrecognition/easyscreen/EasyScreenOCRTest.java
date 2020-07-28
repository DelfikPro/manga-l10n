package mangal10n.textrecognition.easyscreen;

import com.google.inject.Guice;
import mangal10n.config.OcrModule;
import mangal10n.textrecognition.AbstractOcrTest;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EasyScreenOCRTest extends AbstractOcrTest {

	@BeforeClass
	public static void beforeClass() {
		injector = Guice.createInjector(new OcrModule());
	}

	@Before
	public void before() {
		ocr = getInstanceByName(EasyScreenOCR.class.getSimpleName());
	}

	@Test
	public void testSimpleText() throws IOException {
		final String expectedValue = IOUtils.resourceToString("/simple_text_v1.txt", StandardCharsets.UTF_8)
				.replaceAll("\r\n", "\n");
		final byte[] imageBytes = IOUtils.resourceToByteArray("/simple_text.jpg");

		String recognition = ocr.doRecognition(imageBytes);
		assertNotNull(recognition);
		assertEquals(expectedValue, recognition.replaceAll("\r\n", "\n"));
	}
}