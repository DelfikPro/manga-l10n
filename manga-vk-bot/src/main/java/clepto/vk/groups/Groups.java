package clepto.vk.groups;

import clepto.vk.VkModule;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import clepto.vk.VKBot;

@Slf4j
public class Groups extends VkModule {

	public Groups(VKBot bot) {
		super(bot, "groups");
	}

	public LongPollData getLongPollServer() {
		JSONObject json = execute(request("getLongPollServer")
				.param("group_id", getBot().getGroup())
			   );

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
		execute(request("edit")
						.param("group_id", getBot().getGroup())
						.body("description", description)
								 );
	}

}
