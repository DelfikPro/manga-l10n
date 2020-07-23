package clepto.vk.groups;

import clepto.vk.VkModule;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import clepto.vk.VKBot;

import java.util.Properties;

@Slf4j
public class Groups extends VkModule {

	public Groups(VKBot bot) {
		super(bot, "groups");
	}

	public LongPollData getLongPollServer() {
		Properties params = new Properties();
		params.put("group_id", getBot().getGroup());

		JsonObject json = execute(request("getLongPollServer", params));

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
		Properties params = new Properties();
		params.put("group_id", getBot().getGroup());

		Properties appendBody = new Properties();
		appendBody.put("description", description);

		execute(request("edit", params, appendBody));
	}

}
