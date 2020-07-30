package mangal10n.vkbot.translate.impl;

import mangal10n.vkbot.translate.TranslateService;

public class SystranTranslateService implements TranslateService {

	private static final String ROOT_URL = "https://translate.systran.net/translationTools/text?source=zh&target=en&input=";

	@Override
	public String buildUrl(String text) {
		return ROOT_URL + text;
	}
}
