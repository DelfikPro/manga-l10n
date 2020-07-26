package mangal10n.browser;

public interface Request {

	Response execute();

	interface Builder {

		Builder url(String url);

		Builder addQueryParameter(String name, String value);

		default Builder addQueryParameter(String name, Integer value) {
			return addQueryParameter(name, String.valueOf(value));
		}

		default Builder addQueryParameter(String name, Long value) {
			return addQueryParameter(name, String.valueOf(value));
		}

		Builder addHeader(String name, String value);

		Builder addFormData(String name, String value);

		Builder addMultipartData(String name, String value);

		Builder addMultipartData(String name, String filename, String contentType, byte[] rawContent);

		Builder basicAuth(String user, String password);

		Builder get();

		Builder post();

		Builder post(String string);

		Builder post(byte[] bytes);

		Request build();
	}
}
