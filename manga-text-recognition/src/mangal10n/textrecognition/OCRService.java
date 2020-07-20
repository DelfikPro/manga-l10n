package mangal10n.textrecognition;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

@FunctionalInterface
public interface OCRService {

	CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image);

}
