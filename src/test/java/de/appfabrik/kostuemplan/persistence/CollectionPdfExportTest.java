package de.appfabrik.kostuemplan.persistence;

import static org.junit.jupiter.api.Assertions.*;

import de.appfabrik.kostuemplan.application.*;
import de.appfabrik.kostuemplan.domain.*;
import de.appfabrik.kostuemplan.pdf.PdfBoxProjectWriter;
import de.appfabrik.kostuemplan.support.TestImages;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CollectionPdfExportTest {
  @TempDir Path directory;

  @Test
  void exportsAllProjectsWithSeparatePhotosColorsAndPagesWithoutChangingDatabase()
      throws Exception {
    Path database = directory.resolve("collection.db");
    var service = new ProjectService(new SqliteProjectRepository(database));
    service.load();
    service.save(null, project("Zulu cape", "#FF9966", 0x456789));
    service.save(null, project("Alpha dress", "#FFFFFF", 0x987654));
    byte[] before = Files.readAllBytes(database);
    Path target = directory.resolve("collection.pdf");
    new PdfExportService(new PdfBoxProjectWriter())
        .export(service.allSaved(), ExportTarget.inspect(target), false);
    assertArrayEquals(before, Files.readAllBytes(database));
    try (var pdf = Loader.loadPDF(target.toFile())) {
      assertEquals(2, pdf.getNumberOfPages());
      var stripper = new PDFTextStripper();
      stripper.setStartPage(1);
      stripper.setEndPage(1);
      String first = stripper.getText(pdf);
      assertTrue(first.contains("Alpha dress"));
      assertFalse(first.contains("Zulu cape"));
      stripper.setStartPage(2);
      stripper.setEndPage(2);
      String second = stripper.getText(pdf);
      assertTrue(second.contains("Zulu cape"));
      assertTrue(second.contains("#FF9966"));
      for (var page : pdf.getPages()) {
        assertTrue(page.getResources().getXObjectNames().iterator().hasNext());
      }
      var image = new PDFRenderer(pdf).renderImage(1);
      int swatchPixels = 0;
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          if ((image.getRGB(x, y) & 0xffffff) == 0xff9966) swatchPixels++;
        }
      }
      assertTrue(
          swatchPixels > 400,
          "The selected color must be visibly painted, not only printed as a code");
      image.flush();
    }
  }

  @Test
  void laterProjectFailurePreservesExistingPdfAndCleansTemporaryFile() throws Exception {
    var service =
        new ProjectService(new SqliteProjectRepository(directory.resolve("collection.db")));
    service.load();
    service.save(null, project("Alpha cape", "#FF9966", 0x123456));
    service.save(null, new ProjectDraft("Zulu 漢", "", "", "", "", null));
    Path target = directory.resolve("existing.pdf");
    Files.writeString(target, "Previous document");
    var exporter = new PdfExportService(new PdfBoxProjectWriter());
    var inspected = ExportTarget.inspect(target);
    var error =
        assertThrows(
            ExportException.class, () -> exporter.export(service.allSaved(), inspected, true));
    assertEquals(ExportException.Code.UNSUPPORTED_GLYPH, error.code());
    assertEquals("Previous document", Files.readString(target));
    try (var files = Files.list(directory)) {
      assertTrue(
          files.noneMatch(path -> path.getFileName().toString().startsWith(".kostuemplan-")));
    }
  }

  @Test
  void concurrentSaveCannotChangeLaterProjectsInsideTheCollectionSnapshot() throws Exception {
    Path database = directory.resolve("collection.db");
    var service = new ProjectService(new SqliteProjectRepository(database));
    service.load();
    try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        var statement = connection.createStatement()) {
      statement.execute("PRAGMA journal_mode = WAL");
    }
    var alpha = project("Alpha", "#FF9966", 0x123456);
    var original = project("Zulu", "#112233", 0xabcdef);
    service.save(null, alpha);
    var second = service.save(null, original);
    var replacement = project("Changed", "#FFFFFF", 0x000000);
    List<ProjectDraft> snapshot = new ArrayList<>();
    service
        .allSaved()
        .read(
            saved -> {
              snapshot.add(saved);
              if (snapshot.size() == 1) service.save(second.id(), replacement);
            });
    assertEquals(List.of(alpha, original), snapshot);
    assertEquals(replacement, service.saved(second.id()));
  }

  @Test
  void emptyCollectionDoesNotPublishAnEmptyPdf() throws Exception {
    Path target = directory.resolve("empty.pdf");
    var exporter = new PdfExportService(new PdfBoxProjectWriter());
    var inspected = ExportTarget.inspect(target);
    var error =
        assertThrows(
            ExportException.class, () -> exporter.export(receiver -> {}, inspected, false));
    assertEquals(ExportException.Code.EMPTY_COLLECTION, error.code());
    assertFalse(Files.exists(target));
  }

  private static ProjectDraft project(String name, String color, int rgb) throws Exception {
    return new ProjectDraft(
        name,
        "Linen",
        color,
        "Studio",
        "Fitting notes",
        PhotoImporter.decode(TestImages.image("png", 120, 80, rgb)),
        ProjectStatus.STARTED,
        List.of(new ChecklistStep(UUID.randomUUID(), "Cut fabric", true, 0)));
  }
}
