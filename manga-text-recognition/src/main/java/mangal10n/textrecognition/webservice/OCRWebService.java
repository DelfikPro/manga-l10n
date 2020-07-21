package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import mangal10n.textrecognition.OCRService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author func 20.07.2020
 * @project manga-l10n
 */
public class OCRWebService implements OCRService {

	// Service: http://www.ocrwebservice.com/
	private static final String URL = "http://www.ocrwebservice.com/restservices/processDocument?gettext=true&language=english,chinesesimplified,english";

	private final Gson gson = new Gson();
	private List<WebServerUser> users;

	public OCRWebService() {
		try (val bufferedReader = new BufferedReader(new FileReader(new File("tokens.json")))) {
			init(bufferedReader);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public OCRWebService(BufferedReader reader) {
		init(reader);
	}

	@Override
	public String getName() {
		return "Детектор Shibuya";
	}

	@Override
	public String getEmoji() {
		return "\uD83C\uDF71";
	}

	@Override
	public CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image) {
		CompletableFuture<String> future = new CompletableFuture<>();

		System.out.println();
		executorService.submit(() -> {

			try {
				URL url = new URL(URL);

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestMethod("POST");

				for (WebServerUser user : users) {
					connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((user.getUser() + ":" + user.getToken()).getBytes()));

					connection.setRequestProperty("Content-Type", "application/json");
					connection.setRequestProperty("Content-Length", Integer.toString(image.length));

					try (val stream = connection.getOutputStream()) {
						stream.write(image);
						int httpCode = connection.getResponseCode();

						if (httpCode == HttpURLConnection.HTTP_OK) {
							val responseStream = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);

							BufferedReader br = new BufferedReader(responseStream);
							StringBuilder strBuff = new StringBuilder();
							String s;
							while ((s = br.readLine()) != null)
								strBuff.append(s);
							ResponseField response = gson.fromJson(strBuff.toString(), ResponseField.class);

							future.complete(response.getOcrText().stream()
											.map(list -> String.join("  ", list))
											.collect(Collectors.joining("\n"))
										   );
							break;
						} else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
							System.out.println("OCR Error Message: Unauthorizied request");
						}
						connection.disconnect();
					} catch (IOException ex) {
						future.completeExceptionally(ex);
					}
				}
				future.complete("Халява закончилась :L");
			} catch (Exception ex) {
				future.completeExceptionally(ex);
			}

		});
		return future;
	}

	private void init(BufferedReader reader) {
		users = gson.fromJson(
				reader.lines().collect(Collectors.joining("\n")),
				new TypeToken<List<WebServerUser>>() {}.getType());
		users.forEach(System.out::println);
	}
}
