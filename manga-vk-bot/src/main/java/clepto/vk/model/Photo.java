package clepto.vk.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Photo extends Attachment {

	public final int date;
	public final SizeData[] sizes;
	public final int owner_id;
	public final String accessKey;
	public final int album_id;
	public final boolean has_tags;
	public final int id;
	public final String text;

}
