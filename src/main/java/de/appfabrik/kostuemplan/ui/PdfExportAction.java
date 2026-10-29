package de.appfabrik.kostuemplan.ui;

import de.appfabrik.kostuemplan.application.ExportTarget;
import de.appfabrik.kostuemplan.application.PdfExportService;
import de.appfabrik.kostuemplan.application.PdfFileName;
import de.appfabrik.kostuemplan.application.ProjectService;
import de.appfabrik.kostuemplan.domain.Project;
import de.appfabrik.kostuemplan.i18n.AppLanguage;
import de.appfabrik.kostuemplan.i18n.Messages;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.stage.Window;

/** Captures identity once. Filesystem, database and PDF work runs outside the JavaFX thread. */
final class PdfExportAction {
  private final Window owner;
  private final Messages messages;
  private final ProjectService projects;
  private final PdfExportService exporter;
  private final AsyncTasks tasks;
  private final ExportDialogs dialogs;
  private final Consumer<Boolean> busyChanged;
  private final BiConsumer<String, Boolean> status;
  private boolean running;

  PdfExportAction(
      Window owner,
      ProjectService projects,
      PdfExportService exporter,
      AsyncTasks tasks,
      ExportDialogs dialogs,
      Consumer<Boolean> busyChanged,
      BiConsumer<String, Boolean> status) {
    this(
        owner,
        projects,
        exporter,
        tasks,
        dialogs,
        busyChanged,
        status,
        new Messages(AppLanguage.GERMAN));
  }

  PdfExportAction(
      Window owner,
      ProjectService projects,
      PdfExportService exporter,
      AsyncTasks tasks,
      ExportDialogs dialogs,
      Consumer<Boolean> busyChanged,
      BiConsumer<String, Boolean> status,
      Messages messages) {
    this.messages = messages;
    this.owner = owner;
    this.projects = projects;
    this.exporter = exporter;
    this.tasks = tasks;
    this.dialogs = dialogs;
    this.busyChanged = busyChanged;
    this.status = status;
  }

  void start(Project selected) {
    if (selected == null || running) {
      return;
    }
    setRunning(true);
    try {
      var target = dialogs.choose(owner, PdfFileName.suggest(selected.name(), messages));
      if (target == null) {
        finish(messages.text("export.cancelled"), false);
        return;
      }
      status.accept(messages.text("export.preparing"), false);
      tasks.submit(
          () -> ExportTarget.inspect(target),
          inspected -> {
            try {
              boolean approved =
                  inspected.exists() && dialogs.confirmOverwrite(owner, inspected.path());
              if (inspected.exists() && !approved) {
                finish(messages.text("export.cancelled"), false);
                return;
              }
              status.accept(messages.text("export.creating"), false);
              tasks.submit(
                  () -> {
                    exporter.export(projects.saved(selected.id()), inspected, approved);
                    return inspected.path();
                  },
                  path -> finish(messages.text("export.saved", path), false),
                  this::failed);
            } catch (RuntimeException exception) {
              failed(exception);
            }
          },
          this::failed);
    } catch (RuntimeException exception) {
      failed(exception);
    }
  }

  private void failed(Throwable failure) {
    finish(
        UiSupport.message(
            failure instanceof java.io.IOException
                ? PdfExportService.fileFailure(failure)
                : failure,
            messages),
        true);
  }

  private void finish(String message, boolean error) {
    setRunning(false);
    status.accept(message, error);
  }

  private void setRunning(boolean value) {
    running = value;
    busyChanged.accept(value);
  }
}
