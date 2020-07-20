package mangal10n.textrecognition.webservice;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author func 20.07.2020
 * @project manga-l10n
 */
@Getter
@AllArgsConstructor
public class ResponseField {

	@SerializedName("ErrorMessage")
	private final String errorMessage;
	@SerializedName("AvailablePages")
	private final int availablePages;
	@SerializedName("ProcessedPages")
	private final int processedPages;
	@SerializedName("OCRText")
	private final List<List<String>> ocrText;
	@SerializedName("OutputFileUrl")
	private final String outputFileUrl;
	@SerializedName("TaskDescription")
	private final String taskDescription;
	@SerializedName("Reserved")
	private final List<String> reserved;

}
