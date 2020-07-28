package mangal10n.config;

import clepto.vk.LongPoll;
import clepto.vk.Messages;
import clepto.vk.VKBot;
import clepto.vk.groups.Groups;
import clepto.vk.model.Attachment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.name.Named;
import mangal10n.browser.Browser;

import java.util.Map;

public class VkApiModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Gson.class).toProvider(GsonProvider.class).in(Singleton.class);

		bind(Messages.class).toProvider(MessagesProvider.class).in(Singleton.class);
		bind(Groups.class).toProvider(GroupsProvider.class).in(Singleton.class);
		bind(LongPoll.class).toProvider(LongPollProvider.class).in(Singleton.class);
		bind(VKBot.class).toProvider(VkBotProvider.class).in(Singleton.class);
	}

	static class GsonProvider implements Provider<Gson> {

		@Override
		public Gson get() {
			GsonBuilder builder = new GsonBuilder();
			Attachment.registerTypeData(builder);
			return builder.create();
		}
	}

	static class MessagesProvider implements Provider<Messages> {

		@Inject
		private Gson gson;

		@Inject
		private Browser browser;

		@Inject
		@Named("configMap")
		private Map<String, String> config;

		@Override
		public Messages get() {
			return new Messages(gson, browser, config.get("bot-token"));
		}
	}

	static class GroupsProvider implements Provider<Groups> {

		@Inject
		private Gson gson;

		@Inject
		private Browser browser;

		@Inject
		@Named("configMap")
		private Map<String, String> config;

		@Override
		public Groups get() {
			return new Groups(gson, browser, config.get("bot-token"), config.get("bot-id"));
		}
	}

	static class LongPollProvider implements Provider<LongPoll> {

		@Inject
		private Gson gson;

		@Inject
		private Browser browser;

		@Inject
		@Named("configMap")
		private Map<String, String> config;

		@Inject
		private Messages messages;

		@Inject
		private Groups groups;

		@Override
		public LongPoll get() {
			return new LongPoll(gson, browser, config.get("bot-token"), messages, groups);
		}
	}

	static class VkBotProvider implements Provider<VKBot> {

		@Inject
		private Gson gson;

		@Inject
		private Messages messages;

		@Inject
		private Groups groups;

		@Inject
		private LongPoll longPoll;

		@Inject
		@Named("configMap")
		private Map<String, String> config;

		@Override
		public VKBot get() {
			return new VKBot(gson, config.get("bot-id"), config.get("bot-token"), messages, groups, longPoll);
		}
	}
}
