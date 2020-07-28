package mangal10n.config;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;

public class GsonModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Gson.class).toInstance(new Gson());
	}
}
