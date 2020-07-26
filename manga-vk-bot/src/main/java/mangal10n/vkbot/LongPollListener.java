package mangal10n.vkbot;

import com.google.gson.JsonObject;

import java.time.Duration;
import java.util.function.Consumer;

public interface LongPollListener {

	String ANY_EVENT = "__ANY_EVENT__";

	void registerEventHandler(String eventCode, Consumer<JsonObject> handler);

	JsonObject execute(long waitSecond);

	void startListen(Duration wait);

	void startListen();

	boolean isRunning();

	void stopListen();
}
