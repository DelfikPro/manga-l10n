package mangal10n.textrecognition;

public interface OCRService {

	String getName();

	String getEmoji();

	String doRecognition(byte[] image);
}
