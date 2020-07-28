package mangal10n.textrecognition;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import java.util.Map;

public abstract class AbstractOcrTest {

	private static final TypeLiteral<Map<String, OCRService>> TYPE_LITERAL = new TypeLiteral<>(){};
	protected static Injector injector;
	protected OCRService ocr;

	protected OCRService getInstanceByName(String beanName) {
		return injector.getInstance(Key.get(TYPE_LITERAL)).get(beanName);
	}
}
