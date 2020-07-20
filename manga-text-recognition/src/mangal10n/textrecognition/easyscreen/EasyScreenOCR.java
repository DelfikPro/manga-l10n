package mangal10n.textrecognition.easyscreen;

import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;
import mangal10n.textrecognition.OCRService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

		System.out.println("Submitting...");
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

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				publisher.build(stream);
				byte[] rawBody = stream.toByteArray();
				System.out.println(new String(rawBody));
				System.out.println("Created webkit form with boundary '" + publisher.getBoundary() + "'");
				System.out.println("Crafting request: content length is " + rawBody.length);
				Request uploadRequest = new Request("https://online.easyscreenocr.com/Home/Upload", Method.POST)
						.header("content-type", "multipart/form-data; boundary=" + publisher.getBoundary())
						.header("x-requested-with", "XMLHttpRequest")
						.header("Content-Length", String.valueOf(rawBody.length))
						.header("content-length", String.valueOf(rawBody.length))
						.header("content-Length", String.valueOf(rawBody.length))
						.header("Content-Length", String.valueOf(rawBody.length))
						.header("accept", "application/json")
						.header("accept-encoding", "gzip, deflate, br")
						.header("referer", "https://online.easyscreenocr.com/Home/ChineseOCR")
						.header("origin", "https://online.easyscreenocr.com")
						.header("cookie", "__cfduid=d4a659d0110a6c294915e8d2efc2f0b371595235601; _ga=GA1.2.945092593.1595235620; _gid=GA1.2.933254473.1595235620; __atuvc=6%7C30; __atuvs=5f15951789b38942000")
						.header("sec-fetch-dest", "empty")
						.header("sec-fetch-mode",  "cors")
						.header("sec-fetch-site",  "same-origin")
						.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
						.body(rawBody);
				System.out.println("Sending upload request...");
				Response uploadResponse = uploadRequest
						.execute(NO_PROXY);

				System.out.println("Upload response was: " + new String(uploadResponse.getBody()));

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
							ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(downloadResponse.getBody()));
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
					}
				}, 1, 1000, TimeUnit.MILLISECONDS));
			} catch (Exception ex) {
				future.completeExceptionally(ex);
			}
		});


		return future;
	}

}
