package clepto.vk;

import clepto.net.Response;
import lombok.extern.slf4j.Slf4j;

import java.net.Proxy;
import java.util.Random;

@Slf4j
public class Messages extends VkModule {

	private final Random random = new Random();

	public Messages(VKBot bot) {
		super(bot, "messages");
	}

	public void send(int peer, String message) {
		Response response = request("send")
				.param("peer_id", String.valueOf(peer))
				.param("random_id", String.valueOf(random.nextLong()))
				.body("message", message).execute(Proxy.NO_PROXY);
		log.debug(response.toString());
		log.debug(new String(response.getBody()));
	}

}
