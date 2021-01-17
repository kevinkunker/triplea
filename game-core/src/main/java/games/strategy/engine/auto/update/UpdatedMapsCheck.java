package games.strategy.engine.auto.update;

import com.google.common.annotations.VisibleForTesting;
import games.strategy.engine.framework.map.download.DownloadFileDescription;
import games.strategy.engine.framework.map.download.DownloadMapsWindow;
import games.strategy.engine.framework.map.file.system.loader.AvailableMapsIndex;
import games.strategy.engine.framework.map.listing.MapListingFetcher;
import games.strategy.triplea.settings.ClientSetting;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.triplea.swing.SwingComponents;

@UtilityClass
@Slf4j
class UpdatedMapsCheck {

  static final int THRESHOLD_DAYS = 7;

  static boolean isMapUpdateCheckRequired() {
    return isMapUpdateCheckRequired(
        ClientSetting.lastCheckForMapUpdates.getValue().orElse(0L),
        () -> ClientSetting.lastCheckForMapUpdates.setValueAndFlush(Instant.now().toEpochMilli()));
  }

  @VisibleForTesting
  static boolean isMapUpdateCheckRequired(
      final long lastCheckEpochMilli, final Runnable lastCheckSetter) {
    final Instant cutOff = Instant.now().minus(THRESHOLD_DAYS, ChronoUnit.DAYS);
    final Instant lastCheck = Instant.ofEpochMilli(lastCheckEpochMilli);

    lastCheckSetter.run();

    return lastCheck.isBefore(cutOff);
  }

  /** Prompts user to download map updates if maps are out of date. */
  public static void checkDownloadedMapsAreLatest() {
    if (!isMapUpdateCheckRequired()) {
      return;
    }

    // {map name -> version}
    final Map<String, Integer> availableToDownloadMapVersions =
        downloadAvailableMapsListAndComputeAvailableVersions();

    if (availableToDownloadMapVersions.isEmpty()) {
      // A failure happened getting maps. User is already notified.
      return;
    }

    // {map name -> version}
    final Map<String, Integer> installedMapVersions =
        new AvailableMapsIndex().getMapNamesToVersions();

    final Collection<String> outOfDateMapNames =
        computeOutOfDateMaps(installedMapVersions, availableToDownloadMapVersions);

    if (!outOfDateMapNames.isEmpty()) {
      promptUserToUpdateMaps(outOfDateMapNames);
    }
  }

  private static Map<String, Integer> downloadAvailableMapsListAndComputeAvailableVersions() {
    try {
      return MapListingFetcher.getMapDownloadList().stream()
          .collect(
              Collectors.toMap(
                  downloadFileDescription -> normalizeName(downloadFileDescription.getMapName()),
                  DownloadFileDescription::getVersion));
    } catch (final Exception e) {
      log.warn("Failed to getting list of most recent maps", e);
      return Map.of();
    }
  }

  @VisibleForTesting
  static Collection<String> computeOutOfDateMaps(
      final Map<String, Integer> installedMapVersions,
      final Map<String, Integer> availableToDownloadMapVersions) {

    final Collection<String> outOfDateMapNames = new ArrayList<>();

    // Loop over all available maps, check if we have that map present by comparing
    // normalized names, if so, check versions and remember any that are out of date.
    for (final Map.Entry<String, Integer> installedMap : installedMapVersions.entrySet()) {
      final String installedMapName = normalizeName(installedMap.getKey());

      for (final Map.Entry<String, Integer> availableMap :
          availableToDownloadMapVersions.entrySet()) {
        final String availableMapName = normalizeName(availableMap.getKey());
        if (installedMapName.equals(availableMapName)) {

          if (availableMap.getValue() > installedMap.getValue()) {
            outOfDateMapNames.add(availableMap.getKey());
          }
          break;
        }
      }
    }
    return outOfDateMapNames;
  }

  /**
   * Returns a normalized version of the input. Trims off a '.properties' suffix if present,
   * converts to lower case and replaces all spaces with underscores.
   */
  private static String normalizeName(final String inputName) {
    String normalizedName = inputName;
    if (inputName.endsWith(".zip.properties")) {
      normalizedName = inputName.substring(0, inputName.indexOf(".zip.properties"));
    }
    if (normalizedName.endsWith("-master")) {
      normalizedName = inputName.substring(0, inputName.indexOf("-master"));
    }

    normalizedName = normalizedName.replaceAll(" ", "_");
    normalizedName = normalizedName.toLowerCase();

    return normalizedName;
  }

  private static void promptUserToUpdateMaps(final Collection<String> outOfDateMapNames) {
    final StringBuilder text = new StringBuilder();
    text.append(
        "<html>Some of the maps you have are out of date, and newer versions of those "
            + "maps exist.<br><br>");
    text.append("Would you like to update (re-download) the following maps now?<br><ul>");
    for (final String mapName : outOfDateMapNames) {
      text.append("<li> ").append(mapName).append("</li>");
    }
    text.append("</ul></html>");
    SwingComponents.promptUser(
        "Update Your Maps?",
        text.toString(),
        () -> DownloadMapsWindow.showDownloadMapsWindowAndDownload(outOfDateMapNames));
  }
}
