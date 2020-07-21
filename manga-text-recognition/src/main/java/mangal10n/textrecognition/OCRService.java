package mangal10n.textrecognition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public interface OCRService {

	String getName();

	String getEmoji();

	@Deprecated
	CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image);

	default String doRecognition(byte[] image) {
		return null;
	}
}
