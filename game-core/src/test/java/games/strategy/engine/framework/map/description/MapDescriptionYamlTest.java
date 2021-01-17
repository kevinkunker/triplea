package games.strategy.engine.framework.map.description;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MapDescriptionYamlTest {

  @ParameterizedTest
  @MethodSource
  void isValid(final MapDescriptionYaml mapDescriptionYaml) {
    assertThat(mapDescriptionYaml.isValid(), is(true));
  }

  @SuppressWarnings("unused")
  static List<MapDescriptionYaml> isValid() {
    return List.of(
        MapDescriptionYaml.builder()
            .mapName("map name")
            .mapVersion(1)
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder() //
                        .gameName("game name")
                        .xmlPath("path")
                        .build()))
            .build());
  }

  @ParameterizedTest
  @MethodSource
  void isNotValid(final MapDescriptionYaml mapDescriptionYaml) {
    assertThat(mapDescriptionYaml.isValid(), is(false));
  }

  @SuppressWarnings("unused")
  static List<MapDescriptionYaml> isNotValid() {
    return List.of(
        MapDescriptionYaml.builder().build(),
        // no map name
        MapDescriptionYaml.builder()
            .mapVersion(1)
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder()
                        .gameName("game name")
                        .xmlPath("path")
                        .build()))
            .build(),
        // no version
        MapDescriptionYaml.builder() //
            .mapName("no games")
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder()
                        .gameName("game name")
                        .xmlPath("path")
                        .build()))
            .build(),
        // empty game list
        MapDescriptionYaml.builder() //
            .mapName("no games")
            .mapVersion(1)
            .build(),
        // game is missing path
        MapDescriptionYaml.builder()
            .mapName("no version")
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder() //
                        .gameName("game name")
                        .build()))
            .build(),
        // game is missing name
        MapDescriptionYaml.builder()
            .mapName("missing game path")
            .mapVersion(1)
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder() //
                        .xmlPath("path")
                        .build()))
            .build());
  }

  @Test
  void getGameXmlPathByGameName() {
    final MapDescriptionYaml mapDescriptionYaml =
        MapDescriptionYaml.builder()
            .mapName("map name")
            .mapVersion(1)
            .mapGameList(
                List.of(
                    MapDescriptionYaml.MapGame.builder() //
                        .gameName("game1")
                        .xmlPath("path1")
                        .build(),
                    MapDescriptionYaml.MapGame.builder()
                        .gameName("game2")
                        .xmlPath("path2")
                        .build()))
            .build();

    assertThat(mapDescriptionYaml.getGameXmlPathByGameName("game1"), isPresentAndIs("path1"));
    assertThat(mapDescriptionYaml.getGameXmlPathByGameName("game2"), isPresentAndIs("path2"));
    assertThat(
        "game name is not in the game list, looking up the game path by name is empty result.",
        mapDescriptionYaml.getGameXmlPathByGameName("game-DNE"),
        OptionalMatchers.isEmpty());
  }
}
