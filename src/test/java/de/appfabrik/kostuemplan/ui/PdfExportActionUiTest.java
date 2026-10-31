package de.appfabrik.kostuemplan.ui;

import static de.appfabrik.kostuemplan.ui.FxTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import de.appfabrik.kostuemplan.application.*;
import de.appfabrik.kostuemplan.domain.*;
import de.appfabrik.kostuemplan.pdf.PdfBoxProjectWriter;
import de.appfabrik.kostuemplan.persistence.SqliteProjectRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

@EnabledIfSystemProperty(named = "kostuemplan.uiTests", matches = "true")
class PdfExportActionUiTest {
  @TempDir Path directory;
  private ProjectService service;
  private Project first;
  private Stage stage;
  private AsyncTasks tasks;
  private final AtomicBoolean busy = new AtomicBoolean();
  private final AtomicBoolean error = new AtomicBoolean();
  private final AtomicReference<String> message = new AtomicReference<>("");
  private final AtomicInteger choices = new AtomicInteger();
  private final AtomicInteger confirmations = new AtomicInteger();
  private Path choice;
  private boolean approve;

  @BeforeAll
  static void toolkit() throws Exception {
    FxTestSupport.start();
  }

  @BeforeEach
  void setup() throws Exception {
    service = new ProjectService(new SqliteProjectRepository(directory.resolve("kostuemplan.db")));
    service.load();
    first =
        service.save(
            null, new ProjectDraft("Gespeicherter Umhang", "Leinen", "", "", "Notiz", null));
    stage = fx(Stage::new);
    tasks = new AsyncTasks();
  }

  @AfterEach
  void close() throws Exception {
    fx(
        () -> {
          for (var window : List.copyOf(Window.getWindows())) {
            window.hide();
          }
          return null;
        });
    tasks.close();
  }

  @Test
  void cancelledChooserDoesNotReadSaveOrWriteAnything() throws Exception {
    var action = action(new PdfBoxProjectWriter());
    fx(
        () -> {
          action.start(first);
          return null;
        });
    assertFalse(busy.get());
    assertEquals(1, choices.get());
    assertEquals(0, confirmations.get());
    assertEquals(List.of(first), service.load());
    try (var paths = Files.list(directory)) {
      assertEquals(
          List.of("kostuemplan.db"), paths.map(path -> path.getFileName().toString()).toList());
    }
  }

  @Test
  void rejectedOverwriteKeepsOriginal() throws Exception {
    choice = directory.resolve("original.pdf");
    Files.writeString(choice, "Original");
    var action = action(new PdfBoxProjectWriter());
    fx(
        () -> {
          action.start(first);
          return null;
        });
    await(() -> !busy.get());
    assertEquals(1, confirmations.get());
    assertEquals("Original", Files.readString(choice));
    assertFalse(error.get());
    assertTrue(message.get().contains("abgebrochen"));
  }

  @Test
  void approvedOverwriteUsesSavedAggregateAndReportsSuccessOnlyAfterCompletion() throws Exception {
    choice = directory.resolve("original.pdf");
    Files.writeString(choice, "Original");
    approve = true;
    var action = action(new PdfBoxProjectWriter());
    // Stale selection text must not become export data.
    var stale =
        new Project(
            first.id(),
            "Ungespeicherter Titel",
            "",
            "",
            "",
            "Entwurf",
            false,
            ProjectStatus.FINISHED);
    fx(
        () -> {
          action.start(stale);
          return null;
        });
    await(() -> !busy.get());
    assertEquals(1, confirmations.get());
    assertFalse(error.get());
    assertTrue(message.get().startsWith("PDF exportiert:"));
    try (var pdf = Loader.loadPDF(choice.toFile())) {
      String text = new PDFTextStripper().getText(pdf);
      assertTrue(text.contains("Gespeicherter Umhang"));
      assertFalse(text.contains("Entwurf"));
      assertTrue(text.contains("Status: Geplant"));
    }
    assertEquals(List.of(first), service.load());
  }

  @Test
  void concurrentClicksAndSelectionChangesCannotMixProjectsOrBlockUi() throws Exception {
    choice = directory.resolve("selected.pdf");
    var second =
        service.save(null, new ProjectDraft("Anderes Projekt", "", "", "", "Andere Notiz", null));
    var entered = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var action =
        action(
            (snapshot, temporary) -> {
              assertFalse(Platform.isFxApplicationThread());
              entered.countDown();
              try {
                assertTrue(release.await(10, TimeUnit.SECONDS));
              } catch (InterruptedException exception) {
                throw new java.io.IOException(exception);
              }
              new PdfBoxProjectWriter().write(snapshot, temporary);
            });
    try {
      fx(
          () -> {
            action.start(first);
            return null;
          });
      assertTrue(entered.await(10, TimeUnit.SECONDS));
      fx(
          () -> {
            action.start(second);
            return null;
          });
      assertEquals(1, choices.get());
      assertTrue(busy.get());
      assertFalse(message.get().startsWith("PDF exportiert:"));
      assertFalse(Files.exists(choice));
    } finally {
      release.countDown();
    }
    await(() -> !busy.get());
    assertFalse(error.get());
    try (var pdf = Loader.loadPDF(choice.toFile())) {
      String text = new PDFTextStripper().getText(pdf);
      assertTrue(text.contains(first.name()));
      assertFalse(text.contains(second.name()));
    }
  }

  @Test
  void failureIsVisibleAndAllowsRetryWithoutChangingDatabaseOrTarget() throws Exception {
    choice = directory.resolve("original.pdf");
    Files.writeString(choice, "Original");
    approve = true;
    var action =
        action(
            (snapshot, temporary) -> {
              throw new java.io.IOException("File occupied");
            });
    fx(
        () -> {
          action.start(first);
          return null;
        });
    await(() -> !busy.get());
    assertTrue(error.get());
    assertTrue(message.get().contains("Zugriffsrechte"));
    assertEquals("Original", Files.readString(choice));
    assertEquals(List.of(first), service.load());
    var retry = action(new PdfBoxProjectWriter());
    fx(
        () -> {
          retry.start(first);
          return null;
        });
    await(() -> !busy.get());
    assertFalse(error.get());
  }

  @Test
  void mainWindowRequiresSelectedProjectAndEditorHasNoExportAction() throws Exception {
    fx(
        () -> {
          new MainWindow(
                  stage,
                  service,
                  new de.appfabrik.kostuemplan.application.CollectionExport(
                      new SqliteProjectRepository(directory.resolve("kostuemplan.db")),
                      new de.appfabrik.kostuemplan.persistence.JsonCollectionFile()),
                  new de.appfabrik.kostuemplan.application.CollectionImport(
                      new SqliteProjectRepository(directory.resolve("kostuemplan.db")),
                      new de.appfabrik.kostuemplan.persistence.JsonCollectionFile()),
                  tasks,
                  new de.appfabrik.kostuemplan.i18n.Messages(
                      de.appfabrik.kostuemplan.i18n.AppLanguage.GERMAN),
                  new de.appfabrik.kostuemplan.persistence.LanguagePreferences(directory),
                  new de.appfabrik.kostuemplan.persistence.PhotoDirectoryPreferences(directory),
                  "de-DE",
                  false)
              .show();
          return null;
        });
    await(
        () -> uncheckedFx(() -> !((Button) stage.getScene().lookup("#add-project")).isDisabled()));
    fx(
        () -> {
          var export = (Button) stage.getScene().lookup("#export-project");
          assertTrue(export.isDisabled());
          var table = (TableView<?>) stage.getScene().lookup("#projects-table");
          table.getSelectionModel().select(0);
          assertFalse(export.isDisabled());
          table.getSelectionModel().clearSelection();
          assertTrue(export.isDisabled());
          new ProjectEditor(
                  stage,
                  first,
                  null,
                  service,
                  tasks,
                  saved -> fail("Unexpected save"),
                  new de.appfabrik.kostuemplan.i18n.Messages(
                      de.appfabrik.kostuemplan.i18n.AppLanguage.GERMAN),
                  new de.appfabrik.kostuemplan.persistence.PhotoDirectoryPreferences(directory))
              .show();
          var editor =
              Window.getWindows().stream()
                  .filter(window -> window != stage)
                  .findFirst()
                  .orElseThrow();
          assertNull(editor.getScene().lookup("#export-project"));
          return null;
        });
  }

  private PdfExportAction action(PdfWriter writer) throws Exception {
    return fx(
        () ->
            new PdfExportAction(
                stage,
                service,
                new PdfExportService(writer),
                tasks,
                new ExportDialogs() {
                  @Override
                  public Path choose(Window owner, String name) {
                    assertTrue(Platform.isFxApplicationThread());
                    choices.incrementAndGet();
                    assertTrue(name.endsWith(".pdf"));
                    return choice;
                  }

                  @Override
                  public boolean confirmOverwrite(Window owner, Path target) {
                    assertTrue(Platform.isFxApplicationThread());
                    confirmations.incrementAndGet();
                    return approve;
                  }
                },
                busy::set,
                (text, failed) -> {
                  message.set(text);
                  error.set(failed);
                }));
  }
}
