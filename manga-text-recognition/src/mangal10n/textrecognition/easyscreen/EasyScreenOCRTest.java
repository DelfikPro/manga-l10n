package mangal10n.textrecognition.easyscreen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class EasyScreenOCRTest {

	public static void main(String[] args) throws IOException {
		EasyScreenOCR ocr = new EasyScreenOCR();
		ocr.doRecognition(Executors.newSingleThreadScheduledExecutor(), Files.readAllBytes(new File("C:/Users/DelfikPro/Desktop/32.jpg").toPath()))
				.exceptionally(t -> {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					t.printStackTrace(new PrintStream(out));
					return new String(out.toByteArray());
				})
				.thenAccept(System.out::println);
	}

}
