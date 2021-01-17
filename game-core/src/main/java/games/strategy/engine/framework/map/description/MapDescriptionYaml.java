package games.strategy.engine.framework.map.description;

import com.google.common.base.Strings;
import java.io.File;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * POJO data structure representing the contents of a map.yaml file. The data describes which game
 * XML files are in a map, their name, and the download version of the map.
 *
 * <p>Example YAML structure:
 *
 * <pre>
 *   map_name: [string]
 *   version: [number]
 *   games:
 *   - name: [string]
 *     xml_path: [string]
 * </pre>
 *
 * To convert this object to a file, uthe '
 */
@Getter
@ToString
@Slf4j
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class MapDescriptionYaml {

  public static final String MAP_YAML_FILE_NAME = "map.yml";

  interface YamlKeys {
    String MAP_NAME = "map_name";
    String VERSION = "version";
    String GAMES_LIST = "games";
    String GAME_NAME = "name";
    String XML_PATH = "xml_path";
  }

  private final String mapName;
  private final Integer mapVersion;
  private final List<MapGame> mapGameList;

  @Getter
  @ToString
  @Builder
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class MapGame {
    private final String gameName;
    private final String xmlPath;
  }

  /** Dumps (writes) the current data represented in this object into a YAML formatted string. */
  public String toYamlString() {
    return MapDescriptionYamlWriter.toYamlString(this);
  }

  public static Optional<MapDescriptionYaml> fromMap(final File mapZipOrFolder) {
    return MapDescriptionYamlReader.readFromFileOrFolder(mapZipOrFolder);
  }

  boolean isValid() {
    // verify we have map name, version, at least one game, and all games have a name and path
    return !Strings.nullToEmpty(mapName).isBlank()
        && mapVersion != null
        && mapGameList != null
        && !mapGameList.isEmpty()
        && mapGameList.stream()
            .noneMatch(
                game ->
                    Strings.nullToEmpty(game.xmlPath).isBlank()
                        || Strings.nullToEmpty(game.gameName).isBlank());
  }

  public Optional<String> getGameXmlPathByGameName(final String gameName) {
    return mapGameList.stream()
        .filter(map -> map.getGameName().equals(gameName))
        .findAny()
        .map(MapGame::getXmlPath);
  }
}
