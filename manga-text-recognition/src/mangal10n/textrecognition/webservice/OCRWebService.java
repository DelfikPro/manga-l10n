package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import mangal10n.textrecognition.OCRService;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
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
	private List<WebServerUser> users = Collections.singletonList(new WebServerUser("func", "AF1B4B75-94E5-4D50-926C-26DCC1D7508B"));

	private static String getResponseToString(InputStream inputStream) throws IOException {
		InputStreamReader responseStream = new InputStreamReader(inputStream);

		BufferedReader br = new BufferedReader(responseStream);
		StringBuilder strBuff = new StringBuilder();
		String s;
		while ((s = br.readLine()) != null)
			strBuff.append(s);
		return strBuff.toString();
	}

	@Override
	public CompletableFuture<String> doRecognition(ScheduledExecutorService executorService, byte[] image) {
		CompletableFuture<String> future = new CompletableFuture<>();

		WebServerUser user = users.get(0);

		try {
			URL url = new URL(URL);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((user.getUser() + ":" + user.getToken()).getBytes()));

			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", Integer.toString(image.length));

			OutputStream stream = connection.getOutputStream();

			stream.write(image);
			stream.close();

			int httpCode = connection.getResponseCode();

			if (httpCode == HttpURLConnection.HTTP_OK) {
				ResponseField response = gson.fromJson(getResponseToString(connection.getInputStream()), ResponseField.class);

				future.complete(response.getOcrText().stream()
						.map(list -> String.join("  ", list))
						.collect(Collectors.joining("\n"))
				);
			} else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				System.out.println("OCR Error Message: Unauthorizied request");
			}
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return future;
	}
}
