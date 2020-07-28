package mangal10n;

import clepto.vk.Messages;
import clepto.vk.VKBot;
import clepto.vk.model.Attachment;
import clepto.vk.model.Message;
import clepto.vk.model.Photo;
import clepto.vk.model.SizeData;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.config.*;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

	private static Injector injector;
	private static Set<OCRService> ocrServices;

	public static void main(String[] args) {
		log.info("Hello there, fellow traveler.");

		injector = Guice.createInjector(
				new AppModule(),
				new OkHttpBrowserModule(),
				new VkApiModule(),
				new WebServerUserModule(),
				new OcrModule()
		);

		initOcr();

		bot = injector.getInstance(VKBot.class);
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
				Request request = injector.getInstance(Browser.class).requestBuilder()
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
						bot.getMessages().send(peer, builder.toString());
					});
				}
			});
		}
		return null;
	}

	private static void initOcr() {
		TypeLiteral<Map<String, OCRService>> typeLiteral = new TypeLiteral<>(){};
		ocrServices = new HashSet<>(injector.getInstance(Key.get(typeLiteral)).values());
	}
}
