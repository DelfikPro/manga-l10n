package clepto.vk.groups;

import clepto.vk.VkModule;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import clepto.vk.VKBot;

@Slf4j
public class Groups extends VkModule {

	public Groups(VKBot bot) {
		super(bot, "groups");
	}

	public LongPollData getLongPollServer() {
		JsonObject json = execute(request("getLongPollServer")
				.param("group_id", getBot().getGroup())
			   );

		try {
			return new LongPollData(
					json.get("key").getAsString(),
					json.get("server").getAsString(),
					json.get("ts").getAsString()
			);
		} catch (Exception ex) {
			log.debug(json.toString());
			throw ex;
		}

	}

	public void setDescription(String description) {
		execute(request("edit")
						.param("group_id", getBot().getGroup())
						.body("description", description)
								 );
	}

}
