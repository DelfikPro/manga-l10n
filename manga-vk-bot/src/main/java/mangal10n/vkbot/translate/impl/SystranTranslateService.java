package mangal10n.vkbot.translate.impl;

import mangal10n.Language;
import mangal10n.vkbot.translate.TranslateService;

import java.text.MessageFormat;

public class SystranTranslateService implements TranslateService {

	private static final String URL_TEMPLATE = "https://translate.systran.net/translationTools/text?source={0}&target=en&input={1}";

	@Override
	public String buildUrl(Language sourceLang, String text) {
		return MessageFormat.format(URL_TEMPLATE, sourceLang.getCode(), text);
	}
}
