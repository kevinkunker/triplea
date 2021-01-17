package games.strategy.engine.framework.map.description;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class MapDescriptionYamlReaderTest {

  @SuppressWarnings("ConstantConditions")
  @Test
  void readFromFolder() throws Exception {
    final URI sampleFolderUri =
        MapDescriptionYamlParserTest.class
            .getClassLoader()
            .getResource("map_description_yml_parsing/example_directory")
            .toURI();

    final MapDescriptionYaml mapDescriptionYaml =
        MapDescriptionYamlReader.readFromFileOrFolder(new File(sampleFolderUri)).orElseThrow();

    assertThat(mapDescriptionYaml.isValid(), is(true));
  }

  @Test
  void shouldReturnEmptyIfYmlMissingFromFolder() throws Exception {
    final URI sampleFolderUri =
        MapDescriptionYamlParserTest.class
            .getClassLoader()
            .getResource("map_description_yml_parsing/example_missing_yml_directory")
            .toURI();

    final Optional<MapDescriptionYaml> mapDescription =
        MapDescriptionYamlReader.readFromFileOrFolder(new File(sampleFolderUri));

    assertThat(mapDescription, isEmpty());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  void readFromZip() throws Exception {
    final URI sampleZipUri =
        MapDescriptionYamlParserTest.class
            .getClassLoader()
            .getResource("map_description_yml_parsing/example.zip")
            .toURI();

    final MapDescriptionYaml mapDescriptionYaml =
        MapDescriptionYamlReader.readFromFileOrFolder(new File(sampleZipUri)).orElseThrow();

    assertThat(mapDescriptionYaml.isValid(), is(true));
  }

  @Test
  void shouldReturnEmptyIfZipIsMissingYmlFile() throws Exception {
    final URI sampleZipUri =
        MapDescriptionYamlParserTest.class
            .getClassLoader()
            .getResource("map_description_yml_parsing/example-missing-yml.zip")
            .toURI();

    final Optional<MapDescriptionYaml> mapDescription =
        MapDescriptionYamlReader.readFromFileOrFolder(new File(sampleZipUri));

    assertThat(mapDescription, isEmpty());
  }
}
