package mangal10n.textrecognition.webservice;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 20.07.2020
 * @project manga-l10n
 */
@Slf4j
@RequiredArgsConstructor
public class OCRWebService implements OCRService {

	// Service: http://www.ocrwebservice.com/
	private static final String URL = "http://www.ocrwebservice.com/restservices/processDocument?gettext=true&language=english,chinesesimplified,english";

	private final Gson gson;
	private final Browser browser;
	private final List<WebServerUser> users;

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
				Request request = browser.requestBuilder()
						.url(URL)
						.basicAuth(user.getUser(), user.getToken())
						.addHeader("Content-Type", "application/json")
						.post(image)
						.build();

				try (Response response = request.execute()) {
					int httpCode = response.code();
					if (httpCode == HttpURLConnection.HTTP_OK) {
						ResponseField responseField = gson.fromJson(response.body().string(), ResponseField.class);

						return responseField.getOcrText().stream()
								.map(list -> String.join("  ", list))
								.collect(Collectors.joining("\n"))
								.trim();
					} else if (httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
						log.warn("OCR Error Message: Unauthorizied request");
					}
				}
			}
		} catch (IOException e) {
			throw new OCRException(e);
		}

		throw new OCRException("Халява закончилась :L");
	}
}
