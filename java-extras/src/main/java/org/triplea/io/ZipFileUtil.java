package org.triplea.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value(staticConstructor = "fromZip")
public class ZipFileUtil {

  private final File zip;

  /**
   * Finds all game XMLs in a zip file. More specifically, given a zip file, finds all '*.xml' files
   */
  public List<URI> findXmlFiles() {
    try (ZipFile zipFile = new ZipFile(zip);
        FileSystem fileSystem = FileSystems.newFileSystem(zip.toPath(), null)) {

      return zipFile.stream()
          .map(ZipEntry::getName)
          .filter(name -> name.toLowerCase().endsWith(".xml"))
          .map(fileSystem::getPath)
          .map(Path::toUri)
          .collect(Collectors.toList());
    } catch (final IOException e) {
      log.error("Error reading zip file in: " + zip.getAbsolutePath(), e);
    }
    return new ArrayList<>();
  }

  public Optional<InputStream> openFile(final String fileName) {
    try (ZipFile zipFile = new ZipFile(zip)) {
      final ZipEntry zipEntry = zipFile.getEntry(fileName);
      return zipEntry == null ? Optional.empty() : Optional.of(zipFile.getInputStream(zipEntry));
    } catch (final IOException e) {
      log.error("Error opening file: {}, in zip: {}", fileName, zip.getAbsoluteFile(), e);
      return Optional.empty();
    }
  }

  public boolean containsFile(final String fileName) {
    try (ZipFile zipFile = new ZipFile(zip)) {
      return zipFile.getEntry(fileName) != null;
    } catch (final IOException e) {
      log.error("Error reading zip: {}", zip.getAbsoluteFile(), e);
      return false;
    }
  }
}
