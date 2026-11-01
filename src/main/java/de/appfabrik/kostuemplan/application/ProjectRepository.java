package de.appfabrik.kostuemplan.application;

import de.appfabrik.kostuemplan.domain.Photo;
import de.appfabrik.kostuemplan.domain.Project;
import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.util.List;

/** Storage port. Methods complete only after the database operation succeeds. */
public interface ProjectRepository {
  void initialize();

  List<Project> findAll();

  Photo findPhoto(long id);

  /** Complete saved aggregate, read from one consistent database snapshot. */
  ProjectDraft findSaved(long id);

  /** Every project, including photos and steps, from one read-only snapshot. */
  void readSaved(ProjectPdfSource.Receiver receiver) throws java.io.IOException;

  Project create(ProjectDraft draft);

  Project update(long id, ProjectDraft draft);

  void delete(long id);
}
