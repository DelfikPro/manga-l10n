package clepto.vk;

import clepto.net.Method;
import clepto.net.Request;
import clepto.vk.groups.LongPollData;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LongPoll extends VkModule implements Runnable {

	protected String key;
	protected String server;
	protected String ts;

	public interface Handler {
		String handle(int peer, int sender, String message);
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
		System.out.println("VK LongPoll server started successfully.");
		while (true) {

			Request request = new Request((server.startsWith("http") ? "" : "https://") + server, Method.GET);
			request.param("act", "a_check");
			request.param("key", key);
			request.param("ts", ts);
			request.param("wait", "25");
			request.param("mode", "2");

			JSONObject response = execute(request, false);
//			System.out.println(response);


			try {
				String _ts = response.getString("ts");
				JSONArray updates = response.getJSONArray("updates");
				if (updates.length() != 0) processEvent(updates);
				ts = _ts;
				failed = 0;
			} catch (JSONException ex) {
				if (failed > 10) throw new RuntimeException("Не удалось подключиться к LongPoll.");
				else {
					requestLongPollServer();
					failed++;
				}
			}
		}
	}


	private void processEvent(JSONArray array) {
		for (int i = 0; i <  array.length(); ++i) {
			try {
				JSONObject arrayItem = array.getJSONObject(i);
				String eventType = arrayItem.getString("type");
				JSONObject object = arrayItem.getJSONObject("object");
				
				
				switch (eventType) {
					case "message_new":
						JSONObject msg = object.getJSONObject("message");

						String text;
						int from_id;
						int peer_id = msg.getInt("peer_id");
						try {
							from_id = msg.getInt("user_id");
						} catch (Exception ex) {
							from_id = msg.getInt("from_id");
						}
						try {
							text = msg.getString("body");
						} catch (Exception ex) {
							text = msg.getString("text");
						}
						
						lastPeer = peer_id;
						
						text = text.replaceAll("\\[.*]", "");
//						String message = MessageHandler.handle(text, from_id, peer_id);
						if (text.length() > 0 && handler != null) {
							try {
								String apply = handler.handle(peer_id, from_id, text);
								if (apply != null) getBot().messages().send(peer_id, apply);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
				}
				
			} catch (JSONException ignored) {}
		}
	}

}
