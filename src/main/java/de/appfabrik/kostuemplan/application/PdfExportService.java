package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** A completed file is published only after rendering and closing succeed. */
public final class PdfExportService {
  private final PdfWriter writer;
  private final FileCommit commit;

  public PdfExportService(PdfWriter writer) {
    this(writer, PdfExportService::commitFile);
  }

  PdfExportService(PdfWriter writer, FileCommit commit) {
    this.writer = writer;
    this.commit = commit;
  }

  public void export(ProjectDraft saved, ExportTarget target, boolean overwriteConfirmed) {
    export(receiver -> receiver.accept(saved), target, overwriteConfirmed);
  }

  public void export(ProjectPdfSource saved, ExportTarget target, boolean overwriteConfirmed) {
    if (target.exists() && !overwriteConfirmed) {
      throw new ExportException(ExportException.Code.OVERWRITE_REQUIRED);
    }
    Path parent = target.path().getParent();
    if (parent == null) {
      throw new ExportException(ExportException.Code.FOLDER_REQUIRED);
    }
    Path temporary = null;
    try {
      temporary = Files.createTempFile(parent, ".kostuemplan-", ".pdf.tmp");
      writer.write(saved, temporary);
      if (!target.equals(ExportTarget.inspect(target.path()))) {
        throw new ExportException(ExportException.Code.TARGET_CHANGED);
      }
      commit.publish(temporary, target);
    } catch (IOException | SecurityException | UnsupportedOperationException exception) {
      throw fileFailure(exception);
    } finally {
      if (temporary != null) {
        try {
          Files.deleteIfExists(temporary);
        } catch (IOException | SecurityException ignored) {
          // Best effort only. Never replace a successful result with a cleanup error.
        }
      }
    }
  }

  public static ExportException fileFailure(Throwable cause) {
    return new ExportException(ExportException.Code.WRITE_FAILED, cause);
  }

  private static void commitFile(Path temporary, ExportTarget target) throws IOException {
    if (target.exists()) {
      // No non-atomic fallback. An unsupported filesystem must leave the old file intact.
      Files.move(
          temporary,
          target.path(),
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } else {
      // Atomic no-clobber publication, including a file appearing after the last check.
      // Both names refer to the fully closed file until the temporary name is removed.
      Files.createLink(target.path(), temporary);
    }
  }

  @FunctionalInterface
  interface FileCommit {
    void publish(Path temporary, ExportTarget target) throws IOException;
  }
}
