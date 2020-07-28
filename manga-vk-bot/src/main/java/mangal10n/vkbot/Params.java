package mangal10n.vkbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class Params {

	private final Map<String, String> primaryParams;
	private final Map<String, String> secondaryParams;

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Map<String, String> primaryParams;
		private Map<String, String> secondaryParams;

		/**
		 * Параметр, отправляемый в URL метода
		 */
		public Builder addPrimaryParam(String key, String value) {
			if (primaryParams == null) {
				primaryParams = new HashMap<>();
			}

			primaryParams.put(key, value);
			return this;
		}

		public Builder addPrimaryParam(String key, Long value) {
			return addPrimaryParam(key, String.valueOf(value));
		}

		/**
		 * Параметр, отправляемый в теле метода
		 */
		public Builder addSecondaryParam(String key, String value) {
			if (secondaryParams == null) {
				secondaryParams = new HashMap<>();
			}

			secondaryParams.put(key, value);
			return this;
		}

		public Params build() {
			if (primaryParams == null) {
				primaryParams = Collections.emptyMap();
			}

			if (secondaryParams == null) {
				secondaryParams = Collections.emptyMap();
			}

			return new Params(
					Collections.unmodifiableMap(primaryParams),
					Collections.unmodifiableMap(secondaryParams)
			);
		}
	}
}
