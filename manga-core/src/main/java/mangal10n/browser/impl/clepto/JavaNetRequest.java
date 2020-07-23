package mangal10n.browser.impl.clepto;

import lombok.RequiredArgsConstructor;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.browser.impl.BrowserException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RequiredArgsConstructor
public class JavaNetRequest implements Request {

	private final HttpRequest.Builder originRequestBuilder;

	@Override
	public Response execute() {
		try {
			HttpRequest httpRequest = originRequestBuilder.timeout(Duration.ofMinutes(1)).build();
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

			return new JavaNetResponse(httpResponse);
		} catch (InterruptedException | IOException e) {
			throw new BrowserException(e);
		}
	}
}
