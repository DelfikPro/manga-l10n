package mangal10n.textrecognition.webservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author func 20.07.2020
 * @project manga-l10n
 */
@Data
public class WebServerUser {

	private final String user;
	private final String token;

}
