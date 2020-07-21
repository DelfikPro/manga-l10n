package mangal10n.textrecognition.easyscreen;

import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;
import mangal10n.textrecognition.OCRService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.net.Proxy.NO_PROXY;

public class EasyScreenOCR implements OCRService {

	@Override
	public String getName() {
		return "Детектор Okinawa";
	}

	@Override
	public String getEmoji() {
		return "\uD83C\uDF75";
	}

	@Override
	public CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image) {
		CompletableFuture<String> future = new CompletableFuture<>();

		executorService.submit(() -> {
			try {
				final String id = requestId();

				final String resultSendFile = sendFile(id, image);
				System.out.println("[Okinawa] " + resultSendFile);

				final String resultStartConvert = requestStartConvert(id);
				System.out.println("[Okinawa] " + resultStartConvert);

				AtomicReference<ScheduledFuture<?>> task = new AtomicReference<>();
				int[] attemptId = { 1 };
				task.set(executorService.scheduleWithFixedDelay(() -> {
					try {
						String status = requestGetDownloadLink(id);
						System.out.println("[Okinawa] Performing attempt #" + attemptId[0]++ + ": " + status);

						if (status.contains("Fail")) {
							return;
						} else if (status.contains("True")) {
							byte[] body = downloadFile(id);
							String unpackedBody = unpack(body);

							future.complete(unpackedBody);
							task.get().cancel(true);
						} else {
							throw new IllegalStateException("Invalid job status: " + status);
						}
					} catch (Exception ex) {
						future.completeExceptionally(ex);
						task.get().cancel(true);
					}
				}, 1, 1000, TimeUnit.MILLISECONDS));
			} catch (Exception ex) {
				future.completeExceptionally(ex);
			}
		});


		return future;
	}

	private String requestId() {
		Request request = new Request("https://online.easyscreenocr.com/Home/GetNewId", Method.GET);
		return new String(request.execute(NO_PROXY).getBody()).replace("\"", "");
	}

	private String sendFile(String id, byte[] bytes) throws IOException, InterruptedException {
		MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
				.addPart("\"Id\"", id)
				.addPart("\"Index\"", "0")
				.addPart("\"file\"", () -> new ByteArrayInputStream(bytes), "\"32.jpg\"", "image/jpeg");

		HttpRequest uploadRequest = HttpRequest.newBuilder()
				.uri(URI.create("https://online.easyscreenocr.com/Home/Upload"))
				.header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
				.header("x-requested-with", "XMLHttpRequest")
				.timeout(Duration.ofMinutes(1))
				.POST(publisher.buildForJavaNet())
				.build();

		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());

		return uploadResponse.body();
	}

	private String requestStartConvert(String id) {
		Response startResponse = new Request("https://online.easyscreenocr.com/Home/StartConvert", Method.GET)
				.param("Id", id)
				.param("SelectedLanguage", "1")
				.execute(NO_PROXY);

		return new String(startResponse.getBody());
	}

	private String requestGetDownloadLink(String id) {
		Request attempt = new Request("https://online.easyscreenocr.com/Home/GetDownloadLink", Method.GET);
		attempt.param("Id", id);
		Response response = attempt.execute(NO_PROXY);

		return new String(response.getBody());
	}

	private byte[] downloadFile(String id) {
		Request download = new Request("https://online.easyscreenocr.com/UploadedImageForOCR/" + id + "/" + id + ".zip", Method.GET);
		Response downloadResponse = download.execute(NO_PROXY);
		return downloadResponse.getBody();
	}

	private String unpack(byte[] bytes) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry = zip.getNextEntry();
			byte[] buffer = new byte[(int) entry.getSize()];
			int read = zip.read(buffer, 0, buffer.length);
			if (read == 0) {
				throw new IOException("Something went wrong");
			} else {
				return new String(buffer, StandardCharsets.UTF_8);
			}
		}
	}
}
