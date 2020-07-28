package mangal10n.textrecognition.easyscreen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import mangal10n.browser.impl.okhttp.OkHttpBrowser;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@RequiredArgsConstructor
public class EasyScreenOCR implements OCRService {

	private static final int MAX_TRIES = 5;
	private final Browser browser;

	@Override
	public String getName() {
		return "Детектор Okinawa";
	}

	@Override
	public String getEmoji() {
		return "\uD83C\uDF75";
	}

	@Override
	public String doRecognition(byte[] image) {
		try {
			final String id = requestId();

			final String resultSendFile = sendFile(id, image);
			log.debug("[Okinawa] {}", resultSendFile);

			final String resultStartConvert = requestStartConvert(id);
			log.debug("[Okinawa] {}", resultStartConvert);

			int tries = 0;
			String status;
			do {
				status = requestGetDownloadLink(id);
				log.debug("[Okinawa] Performing attempt #{}: {}", tries + 1, status);

				if (!status.contains("True") && !status.contains("False")) {
					throw new OCRException("Invalid job status: " + status);
				} else if (status.contains("True")) {
					break;
				}
				tries++;
			} while (tries < MAX_TRIES);

			if (status.contains("False")) {
				throw new OCRException("Не удалось обработать изображение");
			}

			byte[] body = downloadFile(id);
			return unpack(body).trim();
		} catch (IOException e) {
			throw new OCRException(e);
		}
	}

	private String requestId() throws IOException {
		Request request = browser.requestBuilder()
				.url("https://online.easyscreenocr.com/Home/GetNewId")
				.build();

		try (Response response = request.execute()) {
			return response.body().string().replace("\"", "");
		}
	}

	private String sendFile(String id, byte[] bytes) throws IOException {
		Request request = browser.requestBuilder()
				.url("https://online.easyscreenocr.com/Home/Upload")
				.addHeader("x-requested-with", "XMLHttpRequest")
				.addMultipartData("Id", id)
				.addMultipartData("Index", "0")
				.addMultipartData("file", "32.jpg", "image/jpeg", bytes)
				.build();

		try (Response response = request.execute()) {
			return response.body().string();
		}
	}

	private String requestStartConvert(String id) throws IOException {
		mangal10n.browser.Request request1 = browser.requestBuilder()
				.url("https://online.easyscreenocr.com/Home/StartConvert")
				.addQueryParameter("Id", id)
				.addQueryParameter("SelectedLanguage", "1")
				.build();

		try (Response response = request1.execute()) {
			return response.body().string();
		}
	}

	private String requestGetDownloadLink(String id) throws IOException {
		Request request = browser.requestBuilder()
				.url("https://online.easyscreenocr.com/Home/GetDownloadLink")
				.addQueryParameter("Id", id)
				.build();

		try (Response response = request.execute()) {
			return response.body().string();
		}
	}

	private byte[] downloadFile(String id) throws IOException {
		Request request = browser.requestBuilder()
				.url(MessageFormat.format("https://online.easyscreenocr.com/UploadedImageForOCR/{0}/{0}.zip", id))
				.build();

		try (Response response = request.execute()) {
			return response.body().bytes();
		}
	}

	private String unpack(byte[] bytes) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry = zip.getNextEntry();
			byte[] buffer = new byte[(int) entry.getSize()];
			int read = zip.read(buffer, 0, buffer.length);
			if (read == 0) {
				throw new IOException("Something went wrong");
			} else {
				return new String(buffer, StandardCharsets.UTF_8);
			}
		}
	}
}
