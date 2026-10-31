package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.Project;
import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Exports every saved entry, independent of the current search and selection. */
public final class CollectionExport {
  private final ProjectRepository repository;
  private final CollectionFile files;

  public CollectionExport(ProjectRepository repository, CollectionFile files) {
    this.repository = repository;
    this.files = files;
  }

  public int export(Path target) throws IOException {
    List<Project> saved = repository.findAll();
    Iterable<ProjectDraft> entries =
        () ->
            saved.stream()
                .map(
                    project ->
                        new ProjectDraft(
                            project.name(),
                            project.material(),
                            project.color(),
                            project.location(),
                            project.note(),
                            project.hasPhoto() ? repository.findPhoto(project.id()) : null,
                            project.status(),
                            project.steps()))
                .iterator();
    files.write(target, entries);
    return saved.size();
  }
}
