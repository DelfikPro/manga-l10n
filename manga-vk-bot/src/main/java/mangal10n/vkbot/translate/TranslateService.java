package mangal10n.vkbot.translate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public interface TranslateService {

	String buildUrl(String text);

	static String encode(String text) {
		return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
