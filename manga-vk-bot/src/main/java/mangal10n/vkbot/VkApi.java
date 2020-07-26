package mangal10n.vkbot;

import com.google.gson.JsonObject;

public interface VkApi {

	JsonObject executeMethod(String method, Params params, boolean responseAsIs);

	default JsonObject executeMethod(String method, Params params) {
		return executeMethod(method, params, false);
	}

	LongPollListener createLongPoolListener();
}
