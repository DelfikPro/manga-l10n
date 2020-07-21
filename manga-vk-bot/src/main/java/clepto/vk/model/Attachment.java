package clepto.vk.model;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

public class Attachment {

	@Override
	public String toString() {
		return "Unknown attachment";
	}

	public static void registerTypeData(GsonBuilder builder) {
		builder.registerTypeAdapter(Attachment.class, (JsonDeserializer<Attachment>) (json, type, ctx) -> {
			JsonObject j = json.getAsJsonObject();
			switch (j.get("type").getAsString()) {
				case "photo":
					return ctx.deserialize(j.get("photo"), Photo.class);
				default:
					return new Attachment();
			}
		});
	}

}
