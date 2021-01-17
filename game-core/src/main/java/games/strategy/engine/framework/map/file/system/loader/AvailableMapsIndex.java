package games.strategy.engine.framework.map.file.system.loader;

import com.google.common.annotations.VisibleForTesting;
import games.strategy.engine.ClientFileSystemHelper;
import games.strategy.engine.framework.map.description.MapDescriptionYaml;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.triplea.io.FileUtils;

/** Reads the full set of map descriptions across all downloaded maps. */
@AllArgsConstructor(onConstructor_ = @VisibleForTesting)
public class AvailableMapsIndex {
  // description yaml contents -> map zip or map folder URI (containing the YAML)
  private final Map<MapDescriptionYaml, URI> mapYamlToMapLocation;

  public AvailableMapsIndex() {
    this(computeMapYamlToMapLocation());
  }

  private static Map<MapDescriptionYaml, URI> computeMapYamlToMapLocation() {
    final Map<MapDescriptionYaml, URI> mapping = new HashMap<>();
    // Loop over every file in the maps folder.
    // Look for a 'map.yml' file, if found, add it to mapYamlToMapLocation
    for (final File fileInMapsFolder :
        FileUtils.listFiles(ClientFileSystemHelper.getUserMapsFolder())) {
      MapDescriptionYaml.fromMap(fileInMapsFolder)
          .ifPresent(yaml -> mapping.put(yaml, fileInMapsFolder.toURI()));
    }
    return mapping;
  }

  // TODO: test-me
  public List<String> getSortedGameList() {
    return mapYamlToMapLocation.keySet().stream()
        .map(MapDescriptionYaml::getMapGameList)
        .flatMap(Collection::stream)
        .map(MapDescriptionYaml.MapGame::getGameName)
        .sorted()
        .collect(Collectors.toList());
  }

  // TODO: test-me
  public boolean hasGame(final String gameName) {
    return findGameUriByName(gameName).isPresent();
  }

  /**
   * Returns the path to the file associated with the specified game. Returns empty if there is no
   * game matching the given name.
   *
   * @param gameName The name of the game whose file path is to be retrieved; may be {@code null}.
   * @return The path to the game file; or {@code empty} if the game is not available.
   */
  // TODO: test-me
  public Optional<URI> findGameUriByName(final String gameName) {
    for (final Map.Entry<MapDescriptionYaml, URI> entry : mapYamlToMapLocation.entrySet()) {
      if (entry.getKey().getGameXmlPathByGameName(gameName).isPresent()) {
        final String path = entry.getKey().getGameXmlPathByGameName(gameName).get();
        return Optional.of(URI.create("jar:" + entry.getValue().toString() + "!/" + path));
      }
    }
    return Optional.empty();
  }

  //  // TODO: test-me
  public Map<String, Integer> getMapNamesToVersions() {
    return mapYamlToMapLocation.keySet().stream()
        .collect(
            Collectors.toMap(MapDescriptionYaml::getMapName, MapDescriptionYaml::getMapVersion));
  }

  //  // TODO: test-me
  public Optional<Integer> getMapVersionByName(final String mapName) {
    return mapYamlToMapLocation.keySet().stream()
        .filter(yaml -> yaml.getMapName().equals(mapName))
        .findAny()
        .map(MapDescriptionYaml::getMapVersion);
  }
}
