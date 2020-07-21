package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import mangal10n.textrecognition.OCRException;
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
	public String doRecognition(byte[] image) {
		try {
			for (WebServerUser user : users) {
				HttpURLConnection connection = createConnection(user, image);

				try (val stream = connection.getOutputStream()) {
					stream.write(image);
					int httpCode = connection.getResponseCode();

					if (httpCode == HttpURLConnection.HTTP_OK) {
						String responseString = readString(connection.getInputStream());
						ResponseField response = gson.fromJson(responseString, ResponseField.class);

						return response.getOcrText().stream()
								.map(list -> String.join("  ", list))
								.collect(Collectors.joining("\n"))
								.trim();
					} else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
						System.out.println("OCR Error Message: Unauthorizied request");
					}
				} finally {
					connection.disconnect();
				}
			}
		} catch (IOException e) {
			throw new OCRException(e);
		}

		throw new OCRException("Халява закончилась :L");
	}

	private void init(BufferedReader reader) {
		users = gson.fromJson(
				reader.lines().collect(Collectors.joining("\n")),
				new TypeToken<List<WebServerUser>>() {}.getType());
		users.forEach(System.out::println);
	}

	private HttpURLConnection createConnection(WebServerUser user, byte[] bytes) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((user.getUser() + ":" + user.getToken()).getBytes()));
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

		return connection;
	}

	private String readString(InputStream inputStream) throws IOException {
		val responseStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

		BufferedReader br = new BufferedReader(responseStream);
		StringBuilder strBuff = new StringBuilder();
		String s;
		while ((s = br.readLine()) != null) {
			strBuff.append(s);
		}

		return strBuff.toString();
	}

}
