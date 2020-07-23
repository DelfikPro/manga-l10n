package mangal10n.browser;

import java.io.Closeable;

public interface Response extends Closeable {

	int code();

	Body body();

	interface Body {

		String string();

		byte[] bytes();
	}
}
