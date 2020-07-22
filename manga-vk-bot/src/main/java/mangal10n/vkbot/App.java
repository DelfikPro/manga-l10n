package mangal10n.vkbot;

import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;
import clepto.vk.VKBot;
import clepto.vk.model.Attachment;
import clepto.vk.model.Message;
import clepto.vk.model.Photo;
import clepto.vk.model.SizeData;
import lombok.extern.slf4j.Slf4j;
import mangal10n.ConfigUtils;
import mangal10n.textrecognition.Language;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;
import mangal10n.textrecognition.easyscreen.EasyScreenOCR;
import mangal10n.textrecognition.webservice.OCRWebService;

import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

/**
 * @author func 19.07.2020
 */
@Slf4j
public class App {

	private static VKBot bot;
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static String[] translatorLinks = {
			"https://www.deepl.com/translator#zh/ru/",
			"https://translate.systran.net/translationTools/text?source=zh&target=en&input="
	};

	private static final OCRService[] ocrServices = {
			new OCRWebService(),
			new EasyScreenOCR(),
	};

	public static void main(String[] args) {
		log.info("Hello there, fellow traveler.");

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
				Response response = new Request(size.get().url, Method.GET).execute(Proxy.NO_PROXY);
				for (OCRService ocrService : ocrServices) {
					final CompletableFuture<String> future = new CompletableFuture<>();

					executor.submit(() -> {
						try {
							future.complete(ocrService.doRecognition(response.getBody(), Language.valueOf(message.body))
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

}
