package clepto.vk;

import clepto.vk.groups.LongPollData;
import clepto.vk.model.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.impl.okhttp.OkHttpBrowser;

@Slf4j
public class LongPoll extends VkModule implements Runnable {

	private final Browser browser = new OkHttpBrowser();
	protected String key;
	protected String server;
	protected String ts;

	public interface Handler {
		String handle(int peer, int sender, Message message);
	}

	@Setter
	private Handler handler;
	public volatile long lastPeer;

	private Thread thread;

	public LongPoll(VKBot bot) {
		super(bot, null);
	}

	public void start() {
		(thread = new Thread(this)).start();
	}

	public void requestLongPollServer() {
		LongPollData data = getBot().groups().getLongPollServer();

		key = data.getKey();
		server = data.getServer();
		ts = data.getTs();
	}

	private volatile byte failed = 0;

	public void run() {
		requestLongPollServer();
		log.info("VK LongPoll server started successfully.");
		while (true) {
			Request request = browser.requestBuilder()
					.url((server.startsWith("http") ? "" : "https://") + server)
					.addQueryParameter("act", "a_check")
					.addQueryParameter("key", key)
					.addQueryParameter("ts", ts)
					.addQueryParameter("wait", "25")
					.addQueryParameter("mode", "2")
					.build();

			JsonObject response = execute(request, false);
//			System.out.println(response);


			try {
				String _ts = response.get("ts").getAsString();
				JsonArray updates = response.getAsJsonArray("updates");
				if (updates.size() != 0) processEvent(updates);
				ts = _ts;
				failed = 0;
			} catch (Exception ex) {
				if (failed > 10) throw new RuntimeException("Не удалось подключиться к LongPoll.");
				else {
					requestLongPollServer();
					failed++;
				}
			}
		}
	}


	private void processEvent(JsonArray array) {
		for (int i = 0; i < array.size(); ++i) {
			try {
				JsonObject arrayItem = array.get(i).getAsJsonObject();
				String eventType = arrayItem.get("type").getAsString();
				JsonObject object = arrayItem.getAsJsonObject("object");


				switch (eventType) {
					case "message_new":
						String message1 = object.getAsJsonObject("message").toString();
						Message message = this.getBot().getGson().fromJson(message1, Message.class);
//						System.out.println(message);

						String text = message.body != null ? message.body : message.text;
						int from_id = message.user_id != 0 ? message.user_id : message.from_id;
						int peer_id = message.peer_id;

						lastPeer = peer_id;

						text = text.replaceAll("\\[.*]", "");
//						String message = MessageHandler.handle(text, from_id, peer_id);
						if (handler != null) {
							try {
								String apply = handler.handle(peer_id, from_id, message);
								if (apply != null) getBot().messages().send(peer_id, apply);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
				}
				
			} catch (Exception ignored) {}
		}
	}

}
