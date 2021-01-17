package games.strategy.engine.framework.map.description;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.triplea.test.common.StringToInputStream;

class MapDescriptionYamlWriterTest {

  @DisplayName(
      "Create a MapDescriptionYaml data object, dump it to a string, then reload it "
          + "from string and verify the reloaded value matches the original")
  @Test
  void writeToString() {
    final MapDescriptionYaml original =
        MapDescriptionYaml.builder()
            .mapName("map-name")
            .mapVersion(1)
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder() //
                        .gameName("game0")
                        .xmlPath("path0")
                        .build(),
                    MapDescriptionYaml.MapGame.builder()
                        .gameName("game1")
                        .xmlPath("path1")
                        .build()))
            .build();

    final String stringResult = original.toYamlString();

    final MapDescriptionYaml reload =
        MapDescriptionYamlParser.parse(StringToInputStream.asInputStream(stringResult));
    assertThat("Yaml string: " + stringResult, reload, is(equalTo(original)));
  }
}
