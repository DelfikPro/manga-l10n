package clepto.vk;

import clepto.net.Method;
import clepto.net.Request;
import clepto.net.Response;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.Proxy;

import static lombok.AccessLevel.PROTECTED;

@Slf4j
@RequiredArgsConstructor(access = PROTECTED)
@Getter
public abstract class VkModule {

	private final VKBot bot;
	private final String sectionName;

	protected Request request(String method) {
		Request request = new Request("https://api.vk.com/method/" + sectionName + "." + method, Method.POST);
		request.body("v", "5.103");
		request.body("access_token", bot.getToken());
		return request;
	}

	protected JsonObject execute(Request request) {
		return execute(request, true);
	}

	protected JsonObject execute(Request request, boolean responseSubobject) {
		Response response = request.execute(Proxy.NO_PROXY);
		byte[] body = response.getBody();

		// Создавать строки очень медленно, поэтому будем читать массив байт
		ByteArrayInputStream stream = new ByteArrayInputStream(body);

		// Теперь этот массив в сам объект джсона
		JsonObject json = GlobalBeans.getGson().fromJson(new InputStreamReader(stream), JsonObject.class);

		if (!responseSubobject) return json;

		// А теперь респонс из этого объекта
		try {
			return json.getAsJsonObject("response");
		} catch (Exception ex) {
			log.debug(json.toString());
			throw ex;
		}
	}

}
