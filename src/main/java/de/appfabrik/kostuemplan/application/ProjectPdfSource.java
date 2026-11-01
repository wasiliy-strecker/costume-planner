package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.io.IOException;

/** Streams complete saved projects while their read snapshot remains open. */
@FunctionalInterface
public interface ProjectPdfSource {
  void read(Receiver receiver) throws IOException;

  @FunctionalInterface
  interface Receiver {
    void accept(ProjectDraft saved) throws IOException;
  }
}
