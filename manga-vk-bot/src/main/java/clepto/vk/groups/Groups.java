package clepto.vk.groups;

import clepto.vk.VkModule;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
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

		JSONObject json = execute(request("getLongPollServer", params));

		try {
			return new LongPollData(
					json.getString("key"),
					json.getString("server"),
					json.getString("ts")
			);
		} catch (JSONException ex) {
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
