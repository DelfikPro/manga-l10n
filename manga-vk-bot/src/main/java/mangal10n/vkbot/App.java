package mangal10n.vkbot;

import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;
import clepto.vk.VKBot;
import clepto.vk.model.Attachment;
import clepto.vk.model.Message;
import clepto.vk.model.Photo;
import clepto.vk.model.SizeData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.val;
import mangal10n.ConfigUtils;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;
import mangal10n.textrecognition.easyscreen.EasyScreenOCR;
import mangal10n.textrecognition.webservice.OCRWebService;
import mangal10n.textrecognition.webservice.WebServerUser;

import java.io.*;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author func 19.07.2020
 */
public class App {

	private static VKBot bot;
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static String[] translatorLinks = {
			"https://www.deepl.com/translator#zh/ru/",
			"https://translate.systran.net/translationTools/text?source=zh&target=en&input="
	};

	private static OCRService[] ocrServices;

	public static void main(String[] args) {
		System.out.println("Hello there, fellow traveler.");

		initOcr();

		Map<String, String> config = ConfigUtils.readYamlConfigFromFile("config.yml");
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
		if (message.getAttachments() == null) return null;
		System.out.println(message);
		for (Attachment attachment : message.attachments) {
			if (!(attachment instanceof Photo)) continue;
			Photo photo = (Photo) attachment;
			Optional<SizeData> size = Stream.of(photo.getSizes()).reduce((a, b) -> a.width * a.height > b.width * b.height ? a : b);
			if (size.isEmpty()) return "Странно, не найдено ни одного размера фотки, которую ты скинул!";

			executor.submit(() -> {
				Response response = new Request(size.get().url, Method.GET).execute(Proxy.NO_PROXY);
				for (OCRService ocrService : ocrServices) {
					final CompletableFuture<String> future = new CompletableFuture<>();

					executor.submit(() -> {
						try {
							future.complete(ocrService.doRecognition(response.getBody())
									.replaceAll("[\n\t\r]", " "));
						} catch (OCRException e) {
							future.completeExceptionally(e);
						}
					});

					future.thenAccept(sourceText -> {
						StringBuilder builder = new StringBuilder(ocrService.getEmoji())
								.append(' ')
								.append(ocrService.getName())
								.append(": ")
								.append(sourceText);

						String encoded = URLEncoder.encode(sourceText, StandardCharsets.UTF_8).replace("+", "%20");
						for (String translatorLink : translatorLinks) {
							builder.append('\n')
									.append(ocrService.getEmoji())
									.append(' ')
									.append(translatorLink)
									.append(encoded);
						}
						bot.messages().send(peer, builder.toString());
					});
				}
			});
		}
		return null;
	}

	private static void initOcr() {
		List<WebServerUser> users;
		try (val bufferedReader = new BufferedReader(new FileReader(new File("tokens.json")))) {
			users = new Gson().fromJson(
					bufferedReader.lines().collect(Collectors.joining("\n")),
					new TypeToken<List<WebServerUser>>() {}.getType());
			users.forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
			users = Collections.emptyList();
		}

		//TODO костыльно как-то...
		ocrServices = new OCRService[2];
		ocrServices[0] = new OCRWebService(users);
		ocrServices[1] = new EasyScreenOCR();
	}
}
