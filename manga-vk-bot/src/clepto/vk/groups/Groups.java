package clepto.vk.groups;

import clepto.vk.VkModule;
import org.json.JSONException;
import org.json.JSONObject;
import clepto.vk.VKBot;

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
			System.out.println(json);
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
