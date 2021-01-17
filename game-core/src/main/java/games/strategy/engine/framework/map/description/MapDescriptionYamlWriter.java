package games.strategy.engine.framework.map.description;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;

@UtilityClass
class MapDescriptionYamlWriter {
  String toYamlString(final MapDescriptionYaml mapDescriptionYaml) {
    final Map<String, Object> data = new HashMap<>();
    data.put(MapDescriptionYaml.YamlKeys.MAP_NAME, mapDescriptionYaml.getMapName());
    data.put(MapDescriptionYaml.YamlKeys.VERSION, mapDescriptionYaml.getMapVersion());
    // generate game list
    data.put(
        MapDescriptionYaml.YamlKeys.GAMES_LIST,
        mapDescriptionYaml.getMapGameList().stream()
            .map(MapDescriptionYamlWriter::mapGameToYamlDataMap)
            .collect(Collectors.toList()));

    return new Dump(DumpSettings.builder().build()).dumpToString(data);
  }

  private static Map<String, Object> mapGameToYamlDataMap(final MapDescriptionYaml.MapGame game) {
    final Map<String, Object> gameYamlData = new HashMap<>();
    gameYamlData.put(MapDescriptionYaml.YamlKeys.GAME_NAME, game.getGameName());
    gameYamlData.put(MapDescriptionYaml.YamlKeys.XML_PATH, game.getXmlPath());
    return gameYamlData;
  }
}
