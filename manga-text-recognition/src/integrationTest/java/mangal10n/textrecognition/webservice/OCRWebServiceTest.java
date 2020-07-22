package mangal10n.textrecognition.webservice;

import mangal10n.textrecognition.OCRService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OCRWebServiceTest {

	private OCRService ocr;

	@Before
	public void setUp() {
		final String ocrLogin = System.getProperty("ocrLogin");
		final String ocrToken = System.getProperty("ocrToken");
		System.out.printf("Login: %s | Token: %s", ocrLogin, ocrToken);

		List<WebServerUser> users;
		if (isBlank(ocrLogin) || isBlank(ocrToken)) {
			users = Collections.emptyList();
		} else {
			users = Collections.singletonList(new WebServerUser(ocrLogin, ocrToken));
		}
		ocr = new OCRWebService(users);
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

	private boolean isBlank(String string) {
		return string == null || string.equals("");
	}
}