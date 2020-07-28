package mangal10n.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mangal10n.textrecognition.webservice.WebServerUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WebServerUserModule extends AbstractModule {

	@Override
	protected void configure() {
		TypeLiteral<List<WebServerUser>> typeLiteral = new TypeLiteral<>(){};
		bind(typeLiteral).toProvider(WebServerUserListProvider.class).in(Singleton.class);
	}

	@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
	static class WebServerUserListProvider implements Provider<List<WebServerUser>> {

		private final Gson gson;

		@Override
		public List<WebServerUser> get() {
			List<WebServerUser> users;
			try (val bufferedReader = new BufferedReader(new FileReader(new File("tokens.json")))) {
				users = gson.fromJson(
						bufferedReader.lines().collect(Collectors.joining("\n")),
						new TypeToken<List<WebServerUser>>() {}.getType());
				users.forEach(System.out::println);
			} catch (IOException e) {
				log.error("{}", e.getMessage());
				users = Collections.emptyList();
			}

			return users;
		}
	}
}
