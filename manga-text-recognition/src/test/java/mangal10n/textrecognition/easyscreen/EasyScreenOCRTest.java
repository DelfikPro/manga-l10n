package mangal10n.textrecognition.easyscreen;

import mangal10n.textrecognition.OCRService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class EasyScreenOCRTest {

	private OCRService ocr;

	@Before
	public void setUp() {
		ocr = new EasyScreenOCR();
	}

	@Test
	public void testSimpleText() throws IOException {
		final String expectedValue = "就带她看尽世间繁华 给她山盟海誓。";
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