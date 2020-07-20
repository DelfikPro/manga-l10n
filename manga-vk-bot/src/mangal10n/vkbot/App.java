package mangal10n.vkbot;

import clepto.DataIO;
import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;
import clepto.vk.VKBot;
import clepto.vk.model.Attachment;
import clepto.vk.model.Message;
import clepto.vk.model.Photo;
import clepto.vk.model.SizeData;
import mangal10n.textrecognition.EasyScreenOCR;
import mangal10n.textrecognition.OCRService;

import java.net.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

/**
 * @author func 19.07.2020
 */
public class App {

	private static VKBot bot;
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private static final OCRService ocrService = new EasyScreenOCR();

	public static void main(String[] args) {
		System.out.println("Hello there, fellow traveler.");

		Map<String, String> config = DataIO.readConfig("config.yml");
		if (config.isEmpty()) {
			System.out.println("No configuration file found!");
			return;
		}

		bot = new VKBot(config.get("bot-id"), config.get("bot-token"));
		bot.getLongPoll().setHandler(App::handle);
		bot.getLongPoll().start();
		while (true);
	}

	private static String handle(int peer, int sender, Message message) {
		System.out.println("Hello world!");
		if (message.getAttachments() == null) return null;
		System.out.println(message);
		for (Attachment attachment : message.attachments) {
			System.out.println("Guten tag!");
			if (!(attachment instanceof Photo)) continue;
			Photo photo = (Photo) attachment;
			Optional<SizeData> size = Stream.of(photo.getSizes()).reduce((a, b) -> a.width * a.height > b.width * b.height ? a : b);
			if (size.isEmpty()) return "Странно, не найдено ни одного размера фотки, которую ты скинул!";

			executor.submit(() -> {
				Response response = new Request(size.get().url, Method.GET).execute(Proxy.NO_PROXY);
				ocrService.doRecognition(executor, response.getBody()).thenAccept(str -> {
					bot.messages().send(peer, str);
				});
			});
		}
		return null;
	}

}
