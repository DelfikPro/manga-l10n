package mangal10n.textrecognition;

import mangal10n.Language;

public interface OCRService {

	String getName();

	String getEmoji();

	String doRecognition(byte[] image, Language language);
}
