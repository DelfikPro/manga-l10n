package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import mangal10n.textrecognition.OCRService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

	private Gson gson = new Gson();
	private static final String URL = "http://www.ocrwebservice.com/restservices/processDocument?gettext=true&language=chinesesimplified,english";
	private List<WebServerUser> users;

	@Override
	public CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image) {
		CompletableFuture<String> future = new CompletableFuture<>();

		try (val bufferedReader = new BufferedReader(new FileReader(new File("tokens.json")))) {
			users = gson.fromJson(
					bufferedReader.lines().collect(Collectors.joining("\n")),
					new TypeToken<List<WebServerUser>>() {
					}.getType()
			);

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
						val responseStream = new InputStreamReader(connection.getInputStream());

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
				} catch (IOException ignored) {
				}
			}
		} catch (Exception ignored) {
		}
		return future;
	}
}
