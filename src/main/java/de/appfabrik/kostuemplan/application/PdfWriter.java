package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.io.IOException;
import java.nio.file.Path;

/** Writes a complete immutable saved aggregate to a temporary file. No UI or database access. */
@FunctionalInterface
public interface PdfWriter {
  void write(ProjectDraft saved, Path temporary) throws IOException;
}
