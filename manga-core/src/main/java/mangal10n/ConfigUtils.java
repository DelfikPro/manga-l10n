package mangal10n;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ConfigUtils {
	private final Yaml YAML = new Yaml();

	public Map<String, String> readYamlConfigFromFile(String pathConfig) {
		Path pathToFile = Paths.get(pathConfig);
		if (Files.exists(pathToFile)) {
			try {
				return readYamlConfig(Files.newInputStream(pathToFile));
			} catch (IOException e) {
				e.printStackTrace(); //TODO переместить в логгер
				return Collections.emptyMap();
			}
		} else {
			//TODO в логгер написать что файл не найден
			return Collections.emptyMap();
		}
	}

	public Map<String, String> readYamlConfig(InputStream inputStream) {
		Map<String, Object> rawConfig = YAML.load(inputStream);

		return rawConfig.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().toString()
		));
	}
}
