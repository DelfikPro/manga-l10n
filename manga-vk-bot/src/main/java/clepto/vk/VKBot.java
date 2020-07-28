package clepto.vk;

import clepto.vk.groups.Groups;
import com.google.gson.Gson;
import lombok.Data;

@Data
public class VKBot {

	private final Gson gson;
	private final String group;
	private final String token;
	private final Messages messages;
	private final Groups groups;

	private final LongPoll longPoll;
}
