package clepto.vk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.browser.impl.okhttp.OkHttpBrowser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static lombok.AccessLevel.PROTECTED;

@Slf4j
@RequiredArgsConstructor(access = PROTECTED)
@Getter
public abstract class VkModule {

	private final Gson gson;
	private final Browser browser;
	private final String accessToken;
	private final String sectionName;

	protected Request request(String method, Properties params, Properties appendBody) {
		Request.Builder builder = browser.requestBuilder()
				.url(String.format("https://api.vk.com/method/%s.%s", sectionName, method));

		if (params != null) {
			params.forEach((key, value) -> builder.addQueryParameter(key.toString(), value.toString()));
		}

		builder.addFormData("v", "5.103")
				.addFormData("access_token", accessToken);

		if (appendBody != null) {
			appendBody.forEach((key, value) -> builder.addFormData(key.toString(), value.toString()));
		}

		return builder.build();
	}

	protected Request request(String method, Properties params) {
		return request(method, params, null);
	}

	protected JsonObject execute(Request request) {
		return execute(request, true);
	}

	protected JsonObject execute(Request request, boolean responseSubobject) {
		try (Response response = request.execute()) {
			// Создавать строки очень медленно, поэтому будем читать массив байт
			ByteArrayInputStream stream = new ByteArrayInputStream(response.body().bytes());

			// Теперь этот массив в сам объект джсона
			JsonObject json = gson.fromJson(new InputStreamReader(stream), JsonObject.class);

			if (!responseSubobject) return json;

			// А теперь респонс из этого объекта
			try {
				return json.getAsJsonObject("response");
			} catch (Exception ex) {
				log.debug(json.toString());
				throw ex;
			}
		} catch (IOException e) {
			log.error("{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
