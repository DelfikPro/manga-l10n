package mangal10n;

import clepto.vk.VKBot;
import clepto.vk.model.Attachment;
import clepto.vk.model.Message;
import clepto.vk.model.Photo;
import clepto.vk.model.SizeData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.browser.impl.okhttp.OkHttpBrowser;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;
import mangal10n.textrecognition.easyscreen.EasyScreenOCR;
import mangal10n.textrecognition.webservice.OCRWebService;
import mangal10n.textrecognition.webservice.WebServerUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
@Slf4j
public class App {

	private static final Browser browser = new OkHttpBrowser();
	private static VKBot bot;
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static String[] translatorLinks = {
			"https://www.deepl.com/translator#zh/ru/",
			"https://translate.systran.net/translationTools/text?source=zh&target=en&input="
	};

	private static OCRService[] ocrServices;

	public static void main(String[] args) {
		log.info("Hello there, fellow traveler.");

		initOcr();

		Map<String, String> config = ConfigUtils.readYamlConfigFromFile("config.yml");
		if (config.isEmpty()) {
			log.error("No configuration file found!");
			return;
		}

		bot = new VKBot(config.get("bot-id"), config.get("bot-token"));
		bot.getLongPoll().setHandler(App::handle);
		bot.getLongPoll().start();
		while (true);
	}

	private static String handle(int peer, int sender, Message message) {
		if (message.getAttachments() == null) return null;
		log.debug(message.toString());
		for (Attachment attachment : message.attachments) {
			if (!(attachment instanceof Photo)) continue;
			Photo photo = (Photo) attachment;
			Optional<SizeData> size = Stream.of(photo.getSizes()).reduce((a, b) -> a.width * a.height > b.width * b.height ? a : b);
			if (size.isEmpty()) return "Странно, не найдено ни одного размера фотки, которую ты скинул!";

			executor.submit(() -> {
				Request request = browser.requestBuilder()
						.url(size.get().url)
						.build();

				byte[] bytes;
				try (Response response = request.execute()) {
					bytes = response.body().bytes();
				} catch (IOException e) {
					log.error("{}", e.getMessage(), e);
					return;
				}

				for (OCRService ocrService : ocrServices) {
					final CompletableFuture<String> future = new CompletableFuture<>();

					executor.submit(() -> {
						try {
							future.complete(ocrService.doRecognition(bytes).replaceAll("[\n\t\r]", " "));
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
