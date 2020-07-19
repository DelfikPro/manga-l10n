package mangal10n.vkbot;

import com.petersamokhin.vksdk.core.client.VkApiClient;
import com.petersamokhin.vksdk.core.http.HttpClient;
import com.petersamokhin.vksdk.core.model.VkSettings;
import com.petersamokhin.vksdk.core.model.objects.Message;
import com.petersamokhin.vksdk.http.VkOkHttpClient;

/**
 * @author func 19.07.2020
 */
public class App {

	public static void main(String[] args) {
		int groupId = 197254109;
		String accessToken = "ACCESS";
		HttpClient vkHttpClient = new VkOkHttpClient();

		VkApiClient client = new VkApiClient(groupId, accessToken, VkApiClient.Type.Community, new VkSettings(vkHttpClient));

		client.onMessage(event -> {
			System.out.println(event.getMessage().toString());
			new Message()
					.peerId(event.getMessage().getPeerId())
					.text("Hello, world!")
					.sendFrom(client)
					.execute();
		});

		client.startLongPolling();
	}
}
