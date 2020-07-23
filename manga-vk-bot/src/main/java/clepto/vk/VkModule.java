package clepto.vk;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.browser.impl.okhttp.OkHttpBrowser;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Properties;

import static lombok.AccessLevel.PROTECTED;

@Slf4j
@RequiredArgsConstructor(access = PROTECTED)
@Getter
public abstract class VkModule {

	private final Browser browser = new OkHttpBrowser();
	private final VKBot bot;
	private final String sectionName;

	protected Request request(String method, Properties params, Properties appendBody) {
		Request.Builder builder = browser.requestBuilder()
				.url(String.format("https://api.vk.com/method/%s.%s", sectionName, method));

		if (params != null) {
			params.forEach((key, value) -> builder.addQueryParameter(key.toString(), value.toString()));
		}

		builder.addFormData("v", "5.103")
				.addFormData("access_token", bot.getToken());

		if (appendBody != null) {
			appendBody.forEach((key, value) -> builder.addFormData(key.toString(), value.toString()));
		}

		return builder.build();
	}

	protected Request request(String method, Properties params) {
		return request(method, params, null);
	}

	protected JSONObject execute(Request request) {
		return execute(request, true);
	}

	protected JSONObject execute(Request request, boolean responseSubobject) {
		try (Response response = request.execute()) {
			JSONTokener tokener = new JSONTokener(response.body().byteStream());
			JSONObject json = new JSONObject(tokener);

			if (!responseSubobject) return json;

			try {
				return json.getJSONObject("response");
			} catch (JSONException ex) {
				log.debug(json.toString());
				throw ex;
			}
		} catch (IOException e) {
			//Просто "глушим" излишние try...catch
			throw new RuntimeException(e);
		}
	}

}
