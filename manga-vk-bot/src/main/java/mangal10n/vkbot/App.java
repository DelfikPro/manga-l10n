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
import mangal10n.vkbot.translate.TranslateService;
import mangal10n.vkbot.translate.impl.DeeplTranslateService;
import mangal10n.vkbot.translate.impl.SystranTranslateService;

import java.net.Proxy;
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
	private static final TranslateService[] translateServices = {
			new DeeplTranslateService(),
			new SystranTranslateService()
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
									.replaceAll("[\n\t\r]", " ")
									.replaceAll("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "[ССЫЛКИ НЕ ПЕРЕВОДИМ]")
							);
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

						final String encoded = TranslateService.encode(sourceText);
						for (TranslateService translateService : translateServices) {
							builder.append('\n')
									.append(ocrService.getEmoji())
									.append(' ')
									.append(translateService.buildUrl(encoded));
						}
						bot.messages().send(peer, builder.toString());
					});
				}
			});
		}
		return null;
	}

}
