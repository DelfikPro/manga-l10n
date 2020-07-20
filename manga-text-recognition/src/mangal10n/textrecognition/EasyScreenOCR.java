package mangal10n.textrecognition;

import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
	public CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image) {
		CompletableFuture<String> future = new CompletableFuture<>();

		executorService.submit(() -> {
			try {
				System.out.println("Beginning...");
				Request request = new Request("https://online.easyscreenocr.com/Home/GetNewId", Method.GET);

				String id = new String(request.execute(NO_PROXY).getBody())
						.replace("\"", "");

				MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
						.addPart("\"Id\"", id)
						.addPart("\"Index\"", "0")
						.addPart("\"file\"", () -> new ByteArrayInputStream(image), "\"32.jpg\"", "image/jpeg");

//				ByteArrayOutputStream stream = new ByteArrayOutputStream();
//				publisher.build(stream);
//				byte[] rawBody = stream.toByteArray();
//				System.out.println(new String(rawBody));
				System.out.println("Created webkit form with boundary '" + publisher.getBoundary() + "'");
//				System.out.println("Crafting request: content length is " + rawBody.length);

				HttpRequest uploadRequest = HttpRequest.newBuilder()
						.uri(URI.create("https://online.easyscreenocr.com/Home/Upload"))
						.header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
						.header("x-requested-with", "XMLHttpRequest")
						.timeout(Duration.ofMinutes(1))
						.POST(publisher.buildForJavaNet())
						.build();

				HttpClient client = HttpClient.newHttpClient();
				HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
				System.out.println(uploadResponse.body());

				Response startResponse = new Request("https://online.easyscreenocr.com/Home/StartConvert", Method.GET)
						.param("Id", id)
						.param("SelectedLanguage", "1")
						.execute(NO_PROXY);

				System.out.println(new String(startResponse.getBody()));

				Request attempt = new Request("https://online.easyscreenocr.com/Home/GetDownloadLink", Method.GET);

				attempt.param("Id", id);

				AtomicReference<ScheduledFuture<?>> task = new AtomicReference<>();
				int[] attemptId = {1};
				task.set(executorService.scheduleWithFixedDelay(() -> {
					try {
						Response response = attempt.execute(NO_PROXY);
						String status = new String(response.getBody());
						System.out.println("Performing attempt #" + attemptId[0]++ + ": " + status);
						if (status.contains("Fail")) return;
						if (status.contains("True")) {

							Request download = new Request("https://online.easyscreenocr.com/UploadedImageForOCR/" + id + "/" + id + ".zip", Method.GET);
							Response downloadResponse = download.execute(NO_PROXY);
							byte[] body = downloadResponse.getBody();
							ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(body));
							ZipEntry entry = zip.getNextEntry();
							byte[] buffer = new byte[(int) entry.getSize()];
							int read = zip.read(buffer, 0, buffer.length);
							if (read == 0) throw new IOException("Something went wrong");
							future.complete(new String(buffer, StandardCharsets.UTF_8));
							task.get().cancel(true);
							return;
						}

						throw new IllegalStateException("Invalid job status: " + status);
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

}
