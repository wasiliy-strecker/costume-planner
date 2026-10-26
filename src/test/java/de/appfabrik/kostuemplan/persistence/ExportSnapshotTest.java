package de.appfabrik.kostuemplan.persistence;

import static org.junit.jupiter.api.Assertions.*;

import de.appfabrik.kostuemplan.application.*;
import de.appfabrik.kostuemplan.domain.*;
import de.appfabrik.kostuemplan.pdf.PdfBoxProjectWriter;
import de.appfabrik.kostuemplan.support.TestImages;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportSnapshotTest {
  @TempDir Path directory;

  @Test
  void exportsOwnedPhotoAfterSourceDeletionAndNeverChangesStoredData() throws Exception {
    Path database = directory.resolve("kostuemplan.db");
    var service = new ProjectService(new SqliteProjectRepository(database));
    service.load();
    Path original = directory.resolve("source.jpg");
    Files.write(original, TestImages.image("jpeg", 80, 120, 0x9b3f7f));
    var draft =
        fixture("Gespeicherter Umhang", PhotoImporter.read(original), ProjectStatus.FINISHED);
    var saved = service.save(null, draft);
    Files.delete(original);
    var reopened = new ProjectService(new SqliteProjectRepository(database));
    assertEquals(draft, reopened.saved(saved.id()));
    byte[] before = Files.readAllBytes(database);
    var exporter = new PdfExportService(new PdfBoxProjectWriter());
    Path target = directory.resolve("saved.pdf");
    exporter.export(reopened.saved(saved.id()), ExportTarget.inspect(target), false);
    try (var pdf = Loader.loadPDF(target.toFile())) {
      String text = new PDFTextStripper().getText(pdf);
      assertTrue(text.contains("Gespeicherter Umhang"));
      assertTrue(text.contains("Offen:"));
      assertTrue(pdf.getPage(0).getResources().getXObjectNames().iterator().hasNext());
      assertFalse(text.contains("source.jpg"));
    }
    var failing =
        new PdfExportService(
            (snapshot, temporary) -> {
              throw new java.io.IOException("Synthetic failure");
            });
    var destination = ExportTarget.inspect(target);
    assertThrows(
        ExportException.class, () -> failing.export(reopened.saved(saved.id()), destination, true));
    assertEquals(draft, reopened.saved(saved.id()));
    assertEquals(List.of(saved), reopened.load());
    assertArrayEquals(before, Files.readAllBytes(database));
  }

  @Test
  void snapshotIsImmutableAndUnaffectedByLaterSaveOrOtherProjects() throws Exception {
    var service =
        new ProjectService(new SqliteProjectRepository(directory.resolve("kostuemplan.db")));
    service.load();
    var old =
        fixture(
            "Alter Stand",
            PhotoImporter.decode(TestImages.image("png", 20, 30, 0x0000ff)),
            ProjectStatus.PLANNED);
    var selected = service.save(null, old);
    service.save(null, fixture("Anderes Projekt", null, ProjectStatus.FINISHED));
    var snapshot = service.saved(selected.id());
    var replacement = fixture("Neuer Stand", null, ProjectStatus.STARTED);
    service.save(selected.id(), replacement);
    Path target = directory.resolve("snapshot.pdf");
    new PdfExportService(new PdfBoxProjectWriter())
        .export(snapshot, ExportTarget.inspect(target), false);
    try (var pdf = Loader.loadPDF(target.toFile())) {
      String text = new PDFTextStripper().getText(pdf);
      assertTrue(text.contains("Alter Stand"));
      assertFalse(text.contains("Neuer Stand"));
      assertFalse(text.contains("Anderes Projekt"));
      assertTrue(text.contains("Status: Geplant"));
    }
    assertEquals(replacement, service.saved(selected.id()));
    assertEquals(old, snapshot);
    service.delete(selected.id());
    assertThrows(StorageException.class, () -> service.saved(selected.id()));
  }

  @Test
  void concurrentCommittedChangesCannotMixTextsPhotoStatusAndSteps() throws Exception {
    Path database = directory.resolve("kostuemplan.db");
    var service = new ProjectService(new SqliteProjectRepository(database));
    service.load();
    try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        var statement = connection.createStatement()) {
      statement.execute("PRAGMA journal_mode = WAL");
    }
    var a =
        fixture(
            "Stand A",
            PhotoImporter.decode(TestImages.image("png", 120, 80, 0x00ff00)),
            ProjectStatus.PLANNED);
    var b =
        fixture(
            "Stand B",
            PhotoImporter.decode(TestImages.image("jpeg", 80, 120, 0xff0000)),
            ProjectStatus.FINISHED);
    var project = service.save(null, a);
    try (var executor = Executors.newSingleThreadExecutor()) {
      var writes =
          executor.submit(
              () -> {
                for (int i = 0; i < 100; i++) {
                  service.save(project.id(), i % 2 == 0 ? b : a);
                }
              });
      for (int i = 0; i < 100; i++) {
        var loaded = service.saved(project.id());
        assertTrue(loaded.equals(a) || loaded.equals(b), "Mixed aggregate snapshot");
      }
      writes.get(20, TimeUnit.SECONDS);
    }
  }

  private static ProjectDraft fixture(String name, Photo photo, ProjectStatus status) {
    return new ProjectDraft(
        name,
        name + " Material",
        name + " Farbe",
        name + " Ort",
        name + " Notiz\nZweite Zeile",
        photo,
        status,
        List.of(
            new ChecklistStep(UUID.randomUUID(), name + " Schritt 1", true, 0),
            new ChecklistStep(UUID.randomUUID(), name + " Schritt 2", false, 4)));
  }
}
