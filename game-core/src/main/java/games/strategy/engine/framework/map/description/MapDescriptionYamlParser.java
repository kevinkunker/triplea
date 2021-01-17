package games.strategy.engine.framework.map.description;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Parses a YAML and converts to a POJO. Responsible for converting the raw YAML String data
 * structure into POJO
 */
@UtilityClass
class MapDescriptionYamlParser {

  static MapDescriptionYaml parse(final InputStream inputStream) {
    Preconditions.checkNotNull(inputStream);

    final Load load = new Load(LoadSettings.builder().build());
    final Map<String, Object> yamlData =
        (Map<String, Object>) load.loadFromInputStream(inputStream);

    try {
      return MapDescriptionYaml.builder()
          .mapName(Strings.nullToEmpty((String) yamlData.get(MapDescriptionYaml.YamlKeys.MAP_NAME)))
          .mapVersion((Integer) yamlData.get(MapDescriptionYaml.YamlKeys.VERSION))
          .mapGameList(parseGameList(yamlData))
          .build();
    } catch (final ClassCastException e) {
      return MapDescriptionYaml.builder().build();
    }
  }

  @SuppressWarnings("unchecked")
  private static List<MapDescriptionYaml.MapGame> parseGameList(
      final Map<String, Object> yamlData) {
    final List<Map<String, String>> gameList =
        (List<Map<String, String>>) yamlData.get(MapDescriptionYaml.YamlKeys.GAMES_LIST);

    return gameList == null
        ? List.of()
        : gameList.stream()
            .map(MapDescriptionYamlParser::parseMapGame)
            .collect(Collectors.toList());
  }

  private static MapDescriptionYaml.MapGame parseMapGame(final Map<String, String> yamlData) {
    return MapDescriptionYaml.MapGame.builder()
        .gameName(Strings.nullToEmpty(yamlData.get(MapDescriptionYaml.YamlKeys.GAME_NAME)))
        .xmlPath(Strings.nullToEmpty(yamlData.get(MapDescriptionYaml.YamlKeys.XML_PATH)))
        .build();
  }
}
