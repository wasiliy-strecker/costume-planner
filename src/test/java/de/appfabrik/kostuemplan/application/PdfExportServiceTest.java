package de.appfabrik.kostuemplan.application;

import static org.junit.jupiter.api.Assertions.*;

import de.appfabrik.kostuemplan.domain.ProjectDraft;
import de.appfabrik.kostuemplan.pdf.PdfBoxProjectWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfExportServiceTest {
  @TempDir Path directory;
  private final ProjectDraft saved = new ProjectDraft("Umhang", "", "", "", "Grüße", null);
  private final PdfExportService service = new PdfExportService(new PdfBoxProjectWriter());

  @Test
  void createsAndExplicitlyReplacesCompleteReadablePdf() throws Exception {
    Path target = directory.resolve("project.pdf");
    service.export(saved, ExportTarget.inspect(target), false);
    assertReadable(target);
    Files.writeString(target, "Bestehende Datei");
    service.export(saved, ExportTarget.inspect(target), true);
    assertReadable(target);
    assertNoTemporary();
  }

  @Test
  void unconfirmedOverwriteDoesNotEvenCallRenderer() throws Exception {
    Path target = directory.resolve("project.pdf");
    Files.writeString(target, "Original");
    var called = new AtomicBoolean();
    var exporter = new PdfExportService((snapshot, temp) -> called.set(true));
    var destination = ExportTarget.inspect(target);
    assertThrows(ExportException.class, () -> exporter.export(saved, destination, false));
    assertFalse(called.get());
    assertEquals("Original", Files.readString(target));
    assertNoTemporary();
  }

  @Test
  void renderingFailurePreservesOldFileAndRemovesPartialTemporary() throws Exception {
    Path target = directory.resolve("project.pdf");
    Files.writeString(target, "Original");
    var exporter =
        new PdfExportService(
            (snapshot, temp) -> {
              assertEquals(target.getParent(), temp.getParent());
              Files.writeString(temp, "Teilweise geschrieben");
              throw new IOException("Disk full");
            });
    var destination = ExportTarget.inspect(target);
    assertThrows(ExportException.class, () -> exporter.export(saved, destination, true));
    assertEquals("Original", Files.readString(target));
    assertNoTemporary();
  }

  @Test
  void failedAtomicReplacementPreservesExistingFile() throws Exception {
    Path target = directory.resolve("project.pdf");
    Files.writeString(target, "Original");
    var exporter =
        new PdfExportService(
            new PdfBoxProjectWriter(),
            (temp, destination) -> {
              assertReadable(temp);
              throw new java.nio.file.AccessDeniedException(destination.path().toString());
            });
    var destination = ExportTarget.inspect(target);
    assertThrows(ExportException.class, () -> exporter.export(saved, destination, true));
    assertEquals("Original", Files.readString(target));
    assertNoTemporary();
  }

  @Test
  void newlyAppearedOrChangedTargetIsNeverOverwritten() throws Exception {
    Path target = directory.resolve("project.pdf");
    var destination = ExportTarget.inspect(target);
    var racing =
        new PdfExportService(
            (snapshot, temp) -> {
              new PdfBoxProjectWriter().write(snapshot, temp);
              Files.writeString(target, "Andere Anwendung");
            });
    assertThrows(ExportException.class, () -> racing.export(saved, destination, false));
    var existing = ExportTarget.inspect(target);
    Files.writeString(target, "Inzwischen geändert und länger");
    assertThrows(ExportException.class, () -> service.export(saved, existing, true));
    assertEquals("Inzwischen geändert und länger", Files.readString(target));
    assertNoTemporary();
  }

  @Test
  void invalidDestinationAndUnknownGlyphsLeaveNoNewFile() throws Exception {
    assertThrows(ExportException.class, () -> ExportTarget.inspect(directory));
    var missing = ExportTarget.inspect(directory.resolve("missing/file.pdf"));
    assertThrows(ExportException.class, () -> service.export(saved, missing, false));
    Path target = directory.resolve("project.pdf");
    var destination = ExportTarget.inspect(target);
    var unknown = new ProjectDraft("漢", "", "", "", "", null);
    assertThrows(ExportException.class, () -> service.export(unknown, destination, false));
    assertFalse(Files.exists(target));
    assertNoTemporary();
  }

  @Test
  void existingSymlinkIsRejectedWithoutChangingItsSource() throws Exception {
    org.junit.jupiter.api.Assumptions.assumeFalse(
        System.getProperty("os.name").startsWith("Windows"));
    Path original = directory.resolve("original");
    Files.writeString(original, "Original");
    Path link = directory.resolve("link.pdf");
    Files.createSymbolicLink(link, original);
    assertThrows(ExportException.class, () -> ExportTarget.inspect(link));
    assertEquals("Original", Files.readString(original));
  }

  private static void assertReadable(Path path) throws IOException {
    try (var pdf = Loader.loadPDF(path.toFile())) {
      assertTrue(new PDFTextStripper().getText(pdf).contains("Umhang"));
      assertTrue(new PDFTextStripper().getText(pdf).contains("Grüße"));
    }
  }

  private void assertNoTemporary() throws IOException {
    try (var files = Files.list(directory)) {
      assertTrue(
          files.noneMatch(path -> path.getFileName().toString().startsWith(".kostuemplan-")));
    }
  }
}
