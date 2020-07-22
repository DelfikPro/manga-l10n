package mangal10n.textrecognition.easyscreen;

import lombok.extern.slf4j.Slf4j;
import mangal10n.textrecognition.Language;
import mangal10n.textrecognition.OCRException;
import mangal10n.textrecognition.OCRService;
import okhttp3.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class EasyScreenOCR implements OCRService {

	private static final int MAX_TRIES = 5;
	private Map<Language, Integer> langs = new HashMap<>(){{
		put(Language.ENGLISH, 0);
		put(Language.CHINESE_SIMPLIFIED, 1);
		put(Language.CHINESE_TRADITIONAL, 1);
		put(Language.JAPANESE, 2);
	}};

	@Override
	public String getName() {
		return "Детектор Okinawa";
	}

	@Override
	public String getEmoji() {
		return "\uD83C\uDF75";
	}

	@Override
	public String doRecognition(byte[] image, Language language) {
		try {
			final String id = requestId();

			final String resultSendFile = sendFile(id, image);
			log.debug("[Okinawa] {}", resultSendFile);

			final String resultStartConvert = requestStartConvert(id, language);
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
		okhttp3.Request request = new okhttp3.Request.Builder()
				.url("https://online.easyscreenocr.com/Home/GetNewId")
				.build();

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
		try (okhttp3.Response response = client.newCall(request).execute()) {
			return Objects.requireNonNull(response.body()).string()
					.replace("\"", "");
		}
	}

	private String sendFile(String id, byte[] bytes) throws IOException {
		MultipartBody multipartBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("Id", id)
				.addFormDataPart("Index", "0")
				.addFormDataPart("file", "32.jpg", RequestBody.create(bytes, MediaType.get("image/jpeg")))
				.build();

		okhttp3.Request request = new okhttp3.Request.Builder()
				.url("https://online.easyscreenocr.com/Home/Upload")
				.addHeader("x-requested-with", "XMLHttpRequest")
				.post(multipartBody)
				.build();

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
		try (okhttp3.Response response = client.newCall(request).execute()) {
			return Objects.requireNonNull(response.body()).string();
		}
	}

	@SuppressWarnings("ConstantConditions")
	private String requestStartConvert(String id, Language language) throws IOException {
		final HttpUrl httpUrl = HttpUrl.parse("https://online.easyscreenocr.com/Home/StartConvert")
				.newBuilder()
				.addQueryParameter("Id", id)
				.addQueryParameter("SelectedLanguage", String.valueOf(langs.get(language)))
				.build();

		okhttp3.Request request = new okhttp3.Request.Builder()
				.url(httpUrl)
				.build();

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
		try (okhttp3.Response response = client.newCall(request).execute()) {
			return Objects.requireNonNull(response.body()).string();
		}
	}

	@SuppressWarnings("ConstantConditions")
	private String requestGetDownloadLink(String id) throws IOException {
		final HttpUrl httpUrl = HttpUrl.parse("https://online.easyscreenocr.com/Home/GetDownloadLink")
				.newBuilder()
				.addQueryParameter("Id", id)
				.build();

		okhttp3.Request request = new okhttp3.Request.Builder()
				.url(httpUrl)
				.build();

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
		try (okhttp3.Response response = client.newCall(request).execute()) {
			return Objects.requireNonNull(response.body()).string();
		}
	}

	private byte[] downloadFile(String id) throws IOException {
		okhttp3.Request request = new okhttp3.Request.Builder()
				.url(MessageFormat.format("https://online.easyscreenocr.com/UploadedImageForOCR/{0}/{0}.zip", id))
				.build();

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(Duration.ofMinutes(1))
				.build();
		try (okhttp3.Response response = client.newCall(request).execute()) {
			return Objects.requireNonNull(response.body()).bytes();
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
