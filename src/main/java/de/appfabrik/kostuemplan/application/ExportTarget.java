package de.appfabrik.kostuemplan.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

/** Captured before overwrite confirmation. Rechecked before committing the completed PDF. */
public record ExportTarget(Path path, boolean exists, Object fileKey, long size, Instant modified) {
  public static ExportTarget inspect(Path selected) throws IOException {
    Path path = selected.toAbsolutePath().normalize();
    try {
      var attributes =
          Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (!attributes.isRegularFile()) {
        throw new ExportException(ExportException.Code.REGULAR_FILE_REQUIRED);
      }
      return new ExportTarget(
          path,
          true,
          attributes.fileKey(),
          attributes.size(),
          attributes.lastModifiedTime().toInstant());
    } catch (NoSuchFileException exception) {
      return new ExportTarget(path, false, null, 0, null);
    }
  }
}
