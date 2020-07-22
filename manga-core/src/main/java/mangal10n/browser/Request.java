package mangal10n.browser;

public interface Request {

	Response execute();

	interface Builder {

		Builder url(String url);

		Builder addQueryParameter(String name, String value);

		Builder addHeader(String name, String value);

		Builder addMultipartData(String name, String value);

		Builder addMultipartData(String name, String filename, String contentType, byte[] rawContent);

		Builder get();

		Builder post();

		Builder post(String string);

		Builder post(byte[] bytes);

		Request build();
	}
}
