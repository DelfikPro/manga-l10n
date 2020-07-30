package mangal10n.vkbot.impl.clepto;

import clepto.vk.VKBot;
import clepto.vk.model.Message;
import mangal10n.vkbot.VkBot;

import java.util.function.Consumer;

public class CleptoVkBot implements VkBot {

	private final VKBot bot;
	private Consumer<Message> handler;

	public CleptoVkBot(String botId, String botToken) {
		this.bot = new VKBot(botId, botToken);
	}

	@Override
	public void setIncomingMessageHandler(Consumer<Message> handler) {
		this.handler = handler;
	}

	@Override
	public void sendMessage(int peer, String message) {
		bot.messages().send(peer, message);
	}

	@Override
	public void start() {
		bot.getLongPoll().setHandler((peer, sender, message) -> {
			this.handler.accept(message);
			return null;
		});
		bot.getLongPoll().start();
	}
}
