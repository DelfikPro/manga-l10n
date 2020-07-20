package clepto.vk.model;

import lombok.Data;

import java.util.List;

@Data
public class Message {

	public final int date;
	public final boolean important;
	public final int from_id;
	public final int user_id;
	public final List<Attachment> attachments;
	public final boolean is_hidden;
	public final int id;
	public final String text, body;
	public final long random_id;
	public final int out;
	public final int peer_id;
	public final int conversation_message_id;

}
