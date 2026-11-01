package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.io.IOException;
import java.nio.file.Path;

/** Writes complete saved aggregates to a temporary file without depending on UI or storage. */
@FunctionalInterface
public interface PdfWriter {
  void write(ProjectPdfSource saved, Path temporary) throws IOException;

  default void write(ProjectDraft saved, Path temporary) throws IOException {
    write(receiver -> receiver.accept(saved), temporary);
  }
}
