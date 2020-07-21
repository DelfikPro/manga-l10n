package mangal10n.textrecognition.webservice;

import mangal10n.textrecognition.OCRService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

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
		final String expectedValue = "就带她看尽世间繁华， 给她山盟海誓。";
		final CountDownLatch lock = new CountDownLatch(1);

		byte[] imageBytes = IOUtils.resourceToByteArray("/simple_text.jpg");
		assertNotNull(imageBytes);

		ocr.doRecognition(Executors.newSingleThreadScheduledExecutor(), imageBytes)
				.exceptionally(throwable -> {
					throwable.printStackTrace();
					fail(throwable.getMessage());
					lock.countDown();

					return throwable.getMessage();
				})
				.thenAccept(string -> {
					assertEquals(expectedValue, string);
					lock.countDown();
				});

		try {
			lock.await();
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
}