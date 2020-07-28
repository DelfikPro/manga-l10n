package clepto.vk;

import com.google.gson.Gson;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

@Slf4j
public class Messages extends VkModule {

	private final Random random = new Random();

	@Inject
	public Messages(Gson gson, Browser browser, String accessToken) {
		super(gson, browser, accessToken, "messages");
	}

	public void send(int peer, String message) {
		Properties params = new Properties();
		params.put("peer_id", String.valueOf(peer));
		params.put("random_id", String.valueOf(random.nextLong()));

		Properties appendBody = new Properties();
		appendBody.put("message", message);

		Request request = request("send", params, appendBody);
		try (Response response = request.execute()) {
			log.debug(response.toString());
			log.debug(response.body().string());
		} catch (IOException e) {
			log.error("{}", e.getMessage(), e);
		}
	}

}
