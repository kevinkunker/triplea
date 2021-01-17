package games.strategy.engine.framework.map.description;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.triplea.io.ZipFileUtil;

/**
 * Converts a File to a MapDescriptionYaml data object. This class is responsible to find a
 * 'map.yml' file from a given map folder, map zip, or 'map.yml' file and then open an input stream
 * and begin parsing. Parsing is delegated to {@link MapDescriptionYamlParser}.
 *
 * <p>If no 'map.yml' file can be found, and we are parsing a zip, then a map.yml will be generated
 * (if possible). If the file is a folder, then no map.yml will be generated, presumably it is a map
 * that is in development.
 */
@Slf4j
@UtilityClass
class MapDescriptionYamlReader {
  static Optional<MapDescriptionYaml> readFromFileOrFolder(final File mapZipOrFolder) {
    Preconditions.checkArgument(mapZipOrFolder.exists());

    if (mapZipOrFolder.isDirectory()) {
      // map folder containing a yml file
      return readFromFolder(mapZipOrFolder);
    } else if (mapZipOrFolder.isFile() && mapZipOrFolder.getName().endsWith(".yml")) {
      //  yml file
      return readFromYamlFile(mapZipOrFolder);
    } else if (mapZipOrFolder.isFile() && mapZipOrFolder.getName().endsWith(".zip")) {
      // zip file
      if (ZipFileUtil.fromZip(mapZipOrFolder).containsFile(MapDescriptionYaml.MAP_YAML_FILE_NAME)) {
        // zip contains a .yml file, read that.
        return readFromZipFile(mapZipOrFolder);
      } else if (new File(mapZipOrFolder.getAbsolutePath() + ".yml").exists()) {
        // a generated .yml file exists, read that.
        return readFromYamlFile(new File(mapZipOrFolder.getAbsolutePath() + ".yml"));
      } else {
        // zip file does not contain a map.yml file, generate one.
        final Path fileToWrite = Path.of(mapZipOrFolder.getAbsolutePath() + ".yml");
        return MapDescriptionYamlGenerator.generateYamlFileForMap(mapZipOrFolder, fileToWrite);
      }
    } else {
      return Optional.empty();
    }
  }

  /** Factory method, finds the map.yml file in a folder and reads it. */
  private static Optional<MapDescriptionYaml> readFromFolder(final File folder) {
    Preconditions.checkArgument(folder.isDirectory());

    final Path ymlPath = folder.toPath().resolve(MapDescriptionYaml.MAP_YAML_FILE_NAME);
    if (!ymlPath.toFile().exists()) {
      return Optional.empty();
    }

    try (FileInputStream fileInputStream = new FileInputStream(ymlPath.toFile())) {
      return Optional.of(MapDescriptionYamlParser.parse(fileInputStream)).stream()
          .peek(yaml -> reportSyntaxErrorIfNotValid(ymlPath.toUri(), yaml))
          .filter(MapDescriptionYaml::isValid)
          .findAny();
    } catch (final IOException e) {
      log.error("Error reading file: " + ymlPath.toAbsolutePath());
      return Optional.empty();
    }
  }

  /** Factory method, finds the map.yml file in a zip and reads it. */
  private static Optional<MapDescriptionYaml> readFromYamlFile(final File ymlPath) {
    Preconditions.checkArgument(ymlPath.isFile());
    Preconditions.checkArgument(ymlPath.getName().endsWith(".yml"));

    try (FileInputStream inputStream = new FileInputStream(ymlPath)) {
      return Optional.of(MapDescriptionYamlParser.parse(inputStream)).stream()
          .peek(yaml -> reportSyntaxErrorIfNotValid(ymlPath.toURI(), yaml))
          .filter(MapDescriptionYaml::isValid)
          .findAny();
    } catch (final IOException e) {
      log.error("Error reading file: " + ymlPath.getAbsolutePath(), e);
      return Optional.empty();
    }
  }

  /** Factory method, finds the map.yml file in a zip and reads it. */
  private static Optional<MapDescriptionYaml> readFromZipFile(final File zipFile) {
    Preconditions.checkArgument(zipFile.isFile());

    return ZipFileUtil.fromZip(zipFile)
        .openFile(MapDescriptionYaml.MAP_YAML_FILE_NAME)
        .map(MapDescriptionYamlParser::parse)
        .stream()
        .peek(yaml -> reportSyntaxErrorIfNotValid(zipFile.toURI(), yaml))
        .filter(MapDescriptionYaml::isValid)
        .findAny();
  }

  private static void reportSyntaxErrorIfNotValid(
      final URI fileUri, final MapDescriptionYaml mapDescriptionYaml) {
    if (!mapDescriptionYaml.isValid()) {
      log.warn(
          "Invalid map description YML (map.yml) file detected: {}\n"
              + "Check the file carefully and correct any mistakes.\n"
              + "If this is a map you downloaded, please contact TripleA.\n"
              + "Data parsed:\n"
              + "{}",
          fileUri,
          mapDescriptionYaml.toString());
    }
  }
}
