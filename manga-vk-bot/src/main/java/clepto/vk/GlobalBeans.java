package clepto.vk;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GlobalBeans {

	private Gson gson;

	public Gson getGson() {
		if (gson == null) {
			gson = new Gson();
		}

		return gson;
	}
}
