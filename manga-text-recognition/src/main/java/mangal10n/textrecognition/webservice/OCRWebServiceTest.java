package mangal10n.textrecognition.webservice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.concurrent.Executors;

/**
 * @author func 20.07.2020
 * @project manga-l10n
 */
public class OCRWebServiceTest {
	public static void main(String[] args) throws IOException {
		new OCRWebService().doRecognition(
				Executors.newSingleThreadScheduledExecutor(),
				Files.readAllBytes(new File("C:\\Users\\func\\Desktop\\32.jpg").toPath())
		).exceptionally(t -> {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out));
			return new String(out.toByteArray());
		}).thenAccept(System.out::println);
	}
}
