package mangal10n.textrecognition.webservice;

import mangal10n.textrecognition.Language;
import mangal10n.textrecognition.OCRService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class OCRWebServiceTest {

	private OCRService ocr;

	@Before
	public void setUp() throws IOException {
		InputStream inputStream = IOUtils.resourceToURL("/tokens.json").openStream();
		ocr = new OCRWebService(new BufferedReader(new InputStreamReader(inputStream)));
	}

	@Test
	public void testSimpleText() throws IOException {
		final String expectedValue = IOUtils.resourceToString("/simple_text_v2.txt", StandardCharsets.UTF_8)
				.replaceAll("\r\n", "\n");
		final byte[] imageBytes = IOUtils.resourceToByteArray("/simple_text.jpg");

		String recognition = ocr.doRecognition(imageBytes, Language.CHINESE_SIMPLIFIED);
		assertNotNull(recognition);
		assertEquals(expectedValue, recognition.replaceAll("\r\n", "\n"));
	}
}