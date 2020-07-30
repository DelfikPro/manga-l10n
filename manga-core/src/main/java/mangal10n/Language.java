package mangal10n;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Language {

	RUSSIAN("ru"),
	ENGLISH("en"),
	GERMAN("de"),
	FRENCH("fr"),
	SPANISH("es"),
	PORTUGUESE("pt"),
	ITALIAN("it"),
	DUTCH("nl"),
	POLISH("pl"),
	JAPANESE("ja"),
	CHINESE("zh"),
	CHINESE_TRADITIONAL("zh"),
	CHINESE_SIMPLIFIED("zh");

	private final String code;
}
