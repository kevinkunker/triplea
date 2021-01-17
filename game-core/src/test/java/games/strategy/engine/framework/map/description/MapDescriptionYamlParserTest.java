package games.strategy.engine.framework.map.description;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MapDescriptionYamlParserTest {

  @Test
  void readSampleMapDescription() throws Exception {
    try (InputStream stream =
        MapDescriptionYamlParserTest.class
            .getClassLoader()
            .getResourceAsStream("map_description_yml_parsing/sample_map_description.yml")) {

      final MapDescriptionYaml mapDescriptionYaml = MapDescriptionYamlParser.parse(stream);

      assertThat(mapDescriptionYaml.getMapName(), is("MapName"));
      assertThat(mapDescriptionYaml.getMapVersion(), is(10));
      assertThat(mapDescriptionYaml.getMapGameList(), hasSize(2));
      assertThat(mapDescriptionYaml.getMapGameList().get(0).getGameName(), is("GameName0"));
      assertThat(mapDescriptionYaml.getMapGameList().get(0).getXmlPath(), is("XmlGameFile0"));
      assertThat(mapDescriptionYaml.getMapGameList().get(1).getGameName(), is("GameName1"));
      assertThat(mapDescriptionYaml.getMapGameList().get(1).getXmlPath(), is("XmlGameFile1"));
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "map_description_yml_parsing/invalid_map_description.yml",
        "map_description_yml_parsing/empty_map_description.yml",
        "map_description_yml_parsing/empty_game_list_description.yml"
      })
  void invalidYamls(final String inputFile) throws Exception {
    try (InputStream stream =
        MapDescriptionYamlParserTest.class.getClassLoader().getResourceAsStream(inputFile)) {

      final MapDescriptionYaml mapDescriptionYaml = MapDescriptionYamlParser.parse(stream);
      assertThat(mapDescriptionYaml.isValid(), is(false));
    }
  }
}
