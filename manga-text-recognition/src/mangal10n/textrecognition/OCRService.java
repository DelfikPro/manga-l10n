package mangal10n.textrecognition;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public interface OCRService {

	String getName();

	String getEmoji();

	CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image);

}
