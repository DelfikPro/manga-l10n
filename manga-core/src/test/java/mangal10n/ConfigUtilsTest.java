package mangal10n;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfigUtilsTest {

	@Test
	public void testReadYamlConfig() {
		final String strValue = "String Value";
		final int intValue = 123;
		final String yamlData = "key1: " + strValue + "\nkey2: " + intValue;

		ByteArrayInputStream bais = new ByteArrayInputStream(yamlData.getBytes(StandardCharsets.UTF_8));
		Map<String, String> config = ConfigUtils.readYamlConfig(bais);

		assertFalse(config.isEmpty());
		assertTrue(config.containsKey("key1"));
		assertEquals(strValue, config.get("key1"));
		assertTrue(config.containsKey("key2"));
		assertEquals(intValue, Integer.parseInt(config.get("key2")));
	}
}