package games.strategy.engine.framework.map.description;

import com.google.common.base.Preconditions;
import games.strategy.engine.ClientFileSystemHelper;
import games.strategy.engine.data.GameData;
import games.strategy.engine.data.gameparser.GameParser;
import games.strategy.engine.framework.map.download.DownloadFileProperties;
import games.strategy.triplea.Constants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.triplea.io.ZipFileUtil;
import org.triplea.util.Version;

/**
 * Builds a MapDescriptionYaml file by reading an existing map and corresponding version file. This
 * item is in place to support legacy maps that do not have a map.yml file, for such a case we can
 * generate it.
 *
 * <p>To generate the data file, we need to parse the map to find XMLs, find the game name in that
 * XML, and also we need to parse a map properties file to find the downloaded map version.
 */
@Slf4j
@UtilityClass
class MapDescriptionYamlGenerator {

  /**
   * Writes a 'yml' file for a given map file, if possible, and returns the POJO contents of the
   * file written.
   *
   * @param mapFile An existing map that presumably does not have a 'map.yml' file which we will be
   *     parsing to find game XML file paths, version, and map name.
   */
  static Optional<MapDescriptionYaml> generateYamlFileForMap(
      final File mapFile, final Path fileToWrite) {
    Preconditions.checkArgument(mapFile.exists());
    Preconditions.checkArgument(mapFile.isFile());
    Preconditions.checkArgument(mapFile.getName().endsWith(".zip"));

    try {
      final MapDescriptionYaml generatedYamlData = parseFromMapZip(mapFile);
      final String yamlString = generatedYamlData.toYamlString();

      Files.writeString(fileToWrite, yamlString);
      log.info("Wrote map yaml for file: " + mapFile.getAbsolutePath());
      return Optional.of(generatedYamlData);
    } catch (final IOException e) {
      log.error(
          "Failed to write to directory: "
              + ClientFileSystemHelper.getUserMapsFolder().getAbsolutePath(),
          e);
      return Optional.empty();
    }
  }

  /** Extracts map description data from a map zip by reading XML and zip contents. */
  private static MapDescriptionYaml parseFromMapZip(final File file) {
    final AtomicReference<String> mapName = new AtomicReference<>();

    final Collection<URI> xmlFiles = ZipFileUtil.fromZip(file).findXmlFiles();
    final List<MapDescriptionYaml.MapGame> games =
        xmlFiles.stream()
            .map(
                xmlFile -> {
                  final GameData gameData = GameParser.parse(xmlFile).orElse(null);

                  mapName.set((String) gameData.getProperties().get(Constants.MAP_NAME));

                  final String gameName = gameData.getGameName();

                  final String mapPath = file.toURI().toString() + "!/";
                  final String xmlFullPath = xmlFile.toString();
                  final String xmlRelativePath =
                      xmlFullPath.substring(xmlFullPath.indexOf(mapPath) + mapPath.length());

                  return MapDescriptionYaml.MapGame.builder()
                      .gameName(gameName)
                      .xmlPath(xmlRelativePath)
                      .build();
                })
            .collect(Collectors.toList());

    return MapDescriptionYaml.builder()
        .mapName(mapName.get())
        .mapVersion(parseDownloadVersion(file))
        .mapGameList(games)
        .build();
  }

  private static Integer parseDownloadVersion(final File mapZip) {
    return DownloadFileProperties.loadForZip(mapZip)
        .getVersion()
        .map(Version::new)
        .map(Version::getMajor)
        .orElse(0);
  }
}
