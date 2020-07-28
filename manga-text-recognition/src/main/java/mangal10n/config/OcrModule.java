package mangal10n.config;

import com.google.gson.Gson;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import lombok.RequiredArgsConstructor;
import mangal10n.textrecognition.OCRService;
import mangal10n.textrecognition.easyscreen.EasyScreenOCR;
import mangal10n.textrecognition.webservice.OCRWebService;
import mangal10n.textrecognition.webservice.WebServerUser;

import java.util.Collections;
import java.util.List;

public class OcrModule extends AbstractModule {

	@Override
	protected void configure() {
		MapBinder<String, OCRService> mapBinder = MapBinder.newMapBinder(binder(), String.class, OCRService. class);
		mapBinder.addBinding(EasyScreenOCR.class.getSimpleName()).to(EasyScreenOCR.class).in(Singleton.class);
		mapBinder.addBinding(OCRWebService.class.getSimpleName()).toProvider(OcrWebServiceProvider.class).in(Singleton.class);
	}

	@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
	static class OcrWebServiceProvider implements Provider<OCRService> {

		private final Injector injector;
		private final Gson gson;

		@Override
		public OCRService get() {
			TypeLiteral<List<WebServerUser>> typeLiteral = new TypeLiteral<>(){};
			List<WebServerUser> users;
			try {
				users = injector.getInstance(Key.get(typeLiteral));
			} catch (Exception e) {
				users = Collections.emptyList();
			}

			return new OCRWebService(gson, users);
		}
	}
}
