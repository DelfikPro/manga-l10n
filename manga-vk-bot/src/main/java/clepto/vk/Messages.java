package clepto.vk;

import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Request;
import mangal10n.browser.Response;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

@Slf4j
public class Messages extends VkModule {

	private final Random random = new Random();

	public Messages(VKBot bot) {
		super(bot, "messages");
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
