package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.util.function.Consumer;

/** Supplies validated entries one at a time. Failures must abort the whole import. */
@FunctionalInterface
public interface ProjectImportSource {
  void read(Consumer<ProjectDraft> accept);
}
