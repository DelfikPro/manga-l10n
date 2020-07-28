package mangal10n.config;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import mangal10n.ConfigUtils;

import java.util.Map;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Gson.class).toInstance(new Gson());

		TypeLiteral<Map<String, String>> typeLiteral = new TypeLiteral<>(){};
		bind(typeLiteral).annotatedWith(Names.named("configMap"))
				.toInstance(ConfigUtils.readYamlConfigFromFile("config.yml"));
	}
}
