package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;
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

	public OCRWebService(List<WebServerUser> users) {
		this.users = users;
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
				Request request = new Request.Builder()
						.url(URL)
						.addHeader("Authorization", Credentials.basic(user.getUser(), user.getToken()))
						.addHeader("Content-Type", "application/json")
						.post(RequestBody.create(image))
						.build();

				OkHttpClient client = new OkHttpClient();
				try (Response response1 = client.newCall(request).execute()) {
					int httpCode = response1.code();
					if (httpCode == HttpURLConnection.HTTP_OK) {
						ResponseField response = gson.fromJson(Objects.requireNonNull(response1.body()).string(),
								ResponseField.class);

						return response.getOcrText().stream()
								.map(list -> String.join("  ", list))
								.collect(Collectors.joining("\n"))
								.trim();
					} else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
						System.out.println("OCR Error Message: Unauthorizied request");
					}
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
}
