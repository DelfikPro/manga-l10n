package mangal10n.vkbot.translate.impl;

import mangal10n.Language;
import mangal10n.vkbot.translate.TranslateService;

import java.text.MessageFormat;

public class DeeplTranslateService implements TranslateService {

	private static final String URL_TEMPLATE = "https://www.deepl.com/translator#{0}/ru/{1}";

	@Override
	public String buildUrl(Language sourceLang, String text) {
		return MessageFormat.format(URL_TEMPLATE, sourceLang.getCode(), text);
	}
}
