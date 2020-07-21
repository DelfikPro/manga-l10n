package clepto.vk;

import clepto.vk.groups.Groups;
import clepto.vk.model.Attachment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

@Data
public class VKBot {

	private Gson gson;

	{
		GsonBuilder builder = new GsonBuilder();
		Attachment.registerTypeData(builder);
		this.gson = builder.create();
	}

	private final String group;
	private final String token;

	private LongPoll longPoll;

	private final Messages messages = new Messages(this);
	private final Groups groups = new Groups(this);

	public LongPoll getLongPoll() {
		if (longPoll != null) return longPoll;
		return longPoll = new LongPoll(this);
	}

	public Messages messages() {
		return this.messages;
	}

	public Groups groups() {
		return this.groups;
	}

}
