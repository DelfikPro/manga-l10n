package mangal10n.vkbot;

import clepto.vk.model.Message;

import java.util.function.Consumer;

public interface VkBot {

	void setIncomingMessageHandler(Consumer<Message> handler);

	void sendMessage(int peer, String message);

	void start();
}
