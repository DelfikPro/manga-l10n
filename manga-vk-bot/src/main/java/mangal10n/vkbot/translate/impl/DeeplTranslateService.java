package mangal10n.vkbot.translate.impl;

import mangal10n.vkbot.translate.TranslateService;

public class DeeplTranslateService implements TranslateService {

	private static final String ROOT_URL = "https://www.deepl.com/translator#zh/ru/";

	@Override
	public String buildUrl(String text) {
		return ROOT_URL + text;
	}
}
