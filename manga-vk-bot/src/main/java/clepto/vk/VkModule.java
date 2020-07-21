package clepto.vk;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
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

	private final VKBot bot;
	private final String sectionName;

	@SuppressWarnings("ConstantConditions")
	protected Request request(String method, Properties params, Properties appendBody) {
		HttpUrl httpUrl = HttpUrl.parse("https://api.vk.com/method/" + sectionName + "." + method);
		if (params != null && !params.isEmpty()) {
			HttpUrl.Builder builder = httpUrl.newBuilder();
			params.forEach((key, value) -> builder.addQueryParameter(key.toString(), value.toString()));
			httpUrl = builder.build();
		}

		FormBody.Builder formBuilder = new FormBody.Builder();
		formBuilder
				.add("v", "5.103")
				.add("access_token", bot.getToken());
		if (appendBody != null && !appendBody.isEmpty()) {
			appendBody.forEach((key, value) -> formBuilder.add(key.toString(), value.toString()));
		}
		FormBody formBody = formBuilder.build();

		return new Request.Builder()
				.url(httpUrl)
				.post(formBody)
				.build();
	}

	protected Request request(String method, Properties params) {
		return request(method, params, null);
	}

	protected JSONObject execute(Request request) {
		return execute(request, true);
	}

	@SuppressWarnings("ConstantConditions")
	protected JSONObject execute(Request request, boolean responseSubobject) {
		OkHttpClient client = new OkHttpClient();
		try (Response response = client.newCall(request).execute()) {
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
