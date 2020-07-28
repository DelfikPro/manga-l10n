package clepto.vk.groups;

import clepto.vk.VkModule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import mangal10n.browser.Browser;

import java.util.Properties;

@Slf4j
public class Groups extends VkModule {

	private final String groupId;

	@Inject
	public Groups(Gson gson, Browser browser, String accessToken, String groupId) {
		super(gson, browser, accessToken, "groups");
		this.groupId = groupId;
	}

	public LongPollData getLongPollServer() {
		Properties params = new Properties();
		params.put("group_id", groupId);

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
		params.put("group_id", groupId);

		Properties appendBody = new Properties();
		appendBody.put("description", description);

		execute(request("edit", params, appendBody));
	}

}
