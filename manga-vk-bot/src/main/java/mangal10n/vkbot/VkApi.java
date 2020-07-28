package mangal10n.vkbot;

import com.google.gson.JsonElement;

public interface VkApi {

	JsonElement executeMethod(String method, Params params, boolean responseAsIs);

	default JsonElement executeMethod(String method, Params params) {
		return executeMethod(method, params, false);
	}

	LongPollListener createLongPoolListener();
}
