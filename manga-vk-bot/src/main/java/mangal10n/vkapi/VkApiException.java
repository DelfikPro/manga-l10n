package mangal10n.vkapi;

public class VkApiException extends RuntimeException {

	public VkApiException(Throwable cause) {
		super(cause);
	}

	public VkApiException(String message) {
		super(message);
	}

	public VkApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
