package mangal10n.vkbot;

import com.petersamokhin.vksdk.core.client.VkApiClient;
import com.petersamokhin.vksdk.core.http.HttpClient;
import com.petersamokhin.vksdk.core.model.VkSettings;
import com.petersamokhin.vksdk.core.model.objects.Message;
import com.petersamokhin.vksdk.http.VkOkHttpClient;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * @author func 19.07.2020
 */
public class App {

	public static void main(String[] args) {
		Yaml yaml = new Yaml();
		InputStream inputStream = App.class
				.getClassLoader()
				.getResourceAsStream("vk-config.yml");

		Map<String, Object> values = yaml.load(inputStream);

		HttpClient vkHttpClient = new VkOkHttpClient();

		VkApiClient client = new VkApiClient(
				(int) values.get("group-id"),
				values.get("group-token").toString(),
				VkApiClient.Type.Community, new VkSettings(vkHttpClient)
		);

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
