/*
 * Модифицированная копия VkApiUni.
 * https://gitlab.com/DmitriyMX/vk-api-uni
 */

package mangal10n.vkbot.impl.uni;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.vkbot.LongPollListener;
import mangal10n.vkbot.Params;
import mangal10n.vkbot.VkApi;
import mangal10n.vkbot.VkApiException;

import java.io.IOException;

@RequiredArgsConstructor
public class VkApiUni implements VkApi {

	private static final String VKAPI_URL = "https://api.vk.com/method/";

	private final String apiVersion;
	private final String accessToken;
	private final String groupId;
	private final Browser browser;
	private final Gson gson;

	@Override
	public JsonObject executeMethod(String method, Params params, boolean responaeAsIs) {
		Request.Builder builder = browser.requestBuilder().url(VKAPI_URL + method);
		params.getPrimaryParams().forEach(builder::addQueryParameter);
		builder.addFormData("v", apiVersion).addFormData("access_token", accessToken);
		params.getSecondaryParams().forEach(builder::addFormData);

		try (Response response = builder.build().execute()) {
			checkResponse(response);
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);

			if (responaeAsIs) {
				return jsonObject;
			} else {
				checkJson(jsonObject);
				return jsonObject.getAsJsonObject("response");
			}
		} catch (IOException e) {
			throw new VkApiException(e);
		}
	}

	@Override
	public LongPollListener createLongPoolListener() {
		JsonObject longPollData = executeMethod("groups.getLongPollServer", Params.builder()
				.addSecondaryParam("need_pts", "1")
				.addSecondaryParam("group_id", groupId)
				.build()
		);

		String serverUrl = longPollData.get("server").getAsString();
		if (!serverUrl.startsWith("https://")) {
			serverUrl = "https://" + serverUrl;
		}

		return new LongPollListenerImpl(
				gson,
				browser,
				longPollData.get("key").getAsString(),
				serverUrl,
				longPollData.get("ts").getAsInt()
		);
	}

	private void checkResponse(Response response) {
		if (response.code() != 200) {
			throw new VkApiException("Response code is not OK: " + response.code());
		}

		String header = response.header("Content-Type");
		if (header == null || header.equals("")
				|| (!header.toLowerCase().contains("application/json")
				&& !header.toLowerCase().contains("text/javascript"))) {
			throw new VkApiException("Content type is not JSON/JavaScript: " + header);
		}
	}

	private void checkJson(JsonObject jsonObject) {
		if (jsonObject.has("error")) {
			throw new VkApiException("API ERROR: " + jsonObject.get("error").toString());
		}
	}
}
