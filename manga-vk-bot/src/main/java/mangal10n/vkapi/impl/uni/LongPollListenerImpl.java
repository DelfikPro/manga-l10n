package mangal10n.vkapi.impl.uni;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.vkapi.LongPollListener;
import mangal10n.vkapi.VkApiException;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class LongPollListenerImpl implements LongPollListener {

	private static final int MIN_RECOMEND_SECONDS = 25;

	private final Map<String, Set<Consumer<JsonObject>>> mapHandlers = new HashMap<>();
	private final Gson gson;
	private final Browser browser;
	private final String key;
	private final String server;

	private int ts;
	private long waitSeconds;
	private Thread threadListener;

	public LongPollListenerImpl(Gson gson, Browser browser, String key, String server, int ts) {
		this.gson = gson;
		this.browser = browser;
		this.key = key;
		this.server = server;
		this.ts = ts;
	}

	@Override
	public void registerEventHandler(String eventCode, Consumer<JsonObject> handler) {
		mapHandlers.computeIfAbsent(eventCode, code -> new HashSet<>()).add(handler);
	}

	@Override
	public JsonObject execute(long waitSecond) {
		Request request = browser.requestBuilder()
				.url(server)
				.addQueryParameter("act", "a_check")
				.addQueryParameter("key", key)
				.addQueryParameter("ts", ts)
				.addQueryParameter("wait", waitSecond)
				.addQueryParameter("mode", "2")
				.addQueryParameter("version", "2")
				.build();

		try (Response response = request.execute()) {
			final JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
			ts = jsonObject.get("ts").getAsInt();
			return jsonObject;
		} catch (IOException e) {
			throw new VkApiException(e);
		}
	}

	@Override
	public void startListen(Duration wait) {
		if (isRunning()) {
			throw new VkApiException("LongPollListener is running.");
		}

		long waitSeconds = wait.toSeconds();

		if (waitSeconds < 1) {
			waitSeconds = 1L;
			log.warn("Используется очень низкий интервал запросов! Значение изменено на 1 секунду.");
		}

		if (waitSeconds < MIN_RECOMEND_SECONDS) {
			log.warn("Интервал LongPoll запросов ниже рекомендуемого значения: {} < {}", waitSeconds, MIN_RECOMEND_SECONDS);
		}

		this.waitSeconds = waitSeconds;
		threadListener = new Thread(this::listen, "VK Api Uni - LongPollListener Thread");
		threadListener.start();
	}

	@Override
	public void startListen() {
		startListen(Duration.ofSeconds(MIN_RECOMEND_SECONDS));
	}

	@Override
	public boolean isRunning() {
		return threadListener != null && !threadListener.isInterrupted() && threadListener.isAlive();
	}

	@Override
	public void stopListen() {
		if (isRunning()) {
			threadListener.interrupt();
		}
	}

	private void listen() {
		log.debug("LongPollListener Thread - start.");

		while (!Thread.currentThread().isInterrupted()) {
			final JsonObject jsonObject = this.execute(this.waitSeconds);
			final JsonArray updates = jsonObject.getAsJsonArray("updates");

			updates.forEach(jsonElement -> {
				final JsonObject jsonItem = jsonElement.getAsJsonObject();

				if (mapHandlers.containsKey(ANY_EVENT)) {
					mapHandlers.get(ANY_EVENT).forEach(handler -> handler.accept(jsonItem));
				}

				final String eventCode = jsonItem.get("type").getAsString();
				if (mapHandlers.containsKey(eventCode)) {
					mapHandlers.get(eventCode).forEach(handler -> handler.accept(jsonItem));
				}
			});
		}

		log.debug("LongPollListener Thread - stop.");
		this.threadListener = null;
	}
}
