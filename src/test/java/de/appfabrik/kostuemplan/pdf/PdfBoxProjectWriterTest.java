package de.appfabrik.kostuemplan.pdf;

import static org.junit.jupiter.api.Assertions.*;

import de.appfabrik.kostuemplan.application.ExportException;
import de.appfabrik.kostuemplan.application.PhotoImporter;
import de.appfabrik.kostuemplan.domain.ChecklistStep;
import de.appfabrik.kostuemplan.domain.ProjectDraft;
import de.appfabrik.kostuemplan.domain.ProjectStatus;
import de.appfabrik.kostuemplan.support.TestImages;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PdfBoxProjectWriterTest {
  @TempDir Path directory;
  private final PdfBoxProjectWriter writer = new PdfBoxProjectWriter();

  @ParameterizedTest
  @EnumSource(de.appfabrik.kostuemplan.i18n.AppLanguage.class)
  void translatedPdfPreservesContentImagesAndPageBounds(
      de.appfabrik.kostuemplan.i18n.AppLanguage language) throws Exception {
    var messages = new de.appfabrik.kostuemplan.i18n.Messages(language);
    var photo = PhotoImporter.decode(TestImages.image("png", 320, 180, 0x457c8b));
    var saved =
        new ProjectDraft(
            "Étoile – ação",
            "Leinen & Tüll",
            "Türkis",
            "Kostümkiste 2",
            "Grüße, café, coração\n" + "Lange Notiz. ".repeat(120),
            photo,
            ProjectStatus.STARTED,
            List.of(
                step("Schnitt prüfen – ação", true, 0), step("Schnitt prüfen – ação", false, 2)));
    var output = directory.resolve(language.tag() + ".pdf");
    new PdfBoxProjectWriter(messages).write(saved, output);
    try (var pdf = Loader.loadPDF(output.toFile())) {
      var text = new BoundsStripper().getText(pdf);
      for (var value :
          List.of(
              saved.name(),
              messages.text("pdf.header"),
              messages.text("pdf.status", messages.status(saved.status())),
              messages.text("pdf.material"),
              messages.text("pdf.color"),
              messages.text("pdf.location"),
              messages.text("pdf.note"),
              messages.text("pdf.steps"),
              messages.text("pdf.completed").strip(),
              messages.text("pdf.open").strip(),
              "Schnitt prüfen – ação")) {
        assertTrue(text.contains(value), language.tag() + ": " + value);
      }
      assertEquals(2, text.split("Schnitt prüfen – ação", -1).length - 1);
      var renderer = new PDFRenderer(pdf);
      for (int page = 0; page < pdf.getNumberOfPages(); page++) {
        assertTrue(text.contains(messages.text("pdf.page", page + 1)));
        var image = renderer.renderImageWithDPI(page, 72);
        assertTrue(image.getWidth() > 590);
        image.flush();
      }
      assertTrue(pdf.getPage(0).getResources().getXObjectNames().iterator().hasNext());
    }
    var bad = new ProjectDraft("東京", "", "", "", "", null, ProjectStatus.PLANNED, List.of());
    var error =
        assertThrows(
            ExportException.class,
            () -> new PdfBoxProjectWriter(messages).write(bad, directory.resolve("bad.pdf")));
    assertEquals(ExportException.Code.UNSUPPORTED_GLYPH, error.code());
    assertEquals("U+6771", error.detail());
    assertTrue(
        messages.text("exportError." + error.code().name(), error.detail()).contains("U+6771"));
  }

  @ParameterizedTest
  @EnumSource(ProjectStatus.class)
  void fullProjectEmbedsPhotoFontUnicodeAndIndependentChecks(ProjectStatus status)
      throws Exception {
    var photo = PhotoImporter.decode(TestImages.image("png", 320, 180, 0x457c8b));
    var saved =
        new ProjectDraft(
            "Frühlingselfe – Größe ÄÖÜ ß",
            "Leinen & Tüll",
            "Türkis",
            "Kostümkiste 2",
            "Erste Zeile: „Schön“ • 25 € ± ½\n\nLetzte Zeile: Gru\u0308ße!",
            photo,
            status,
            List.of(step("Schnitt prüfen", true, 1), step("Saum nähen", false, 9)));
    Path output = directory.resolve("full.pdf");
    writer.write(saved, output);
    try (var pdf = Loader.loadPDF(output.toFile())) {
      assertEquals(1, pdf.getNumberOfPages());
      var text = new BoundsStripper().getText(pdf);
      for (var expected :
          List.of(
              saved.name(),
              "Leinen & Tüll",
              "Türkis",
              "Kostümkiste 2",
              "„Schön“ • 25 € ± ½",
              "Letzte Zeile: Grüße!",
              "Status: "
                  + new de.appfabrik.kostuemplan.i18n.Messages(
                          de.appfabrik.kostuemplan.i18n.AppLanguage.GERMAN)
                      .status(status),
              "☑ Erledigt: Schnitt prüfen",
              "☐ Offen: Saum nähen")) {
        assertTrue(text.contains(expected), text);
      }
      for (var step : saved.steps()) {
        assertFalse(text.contains(step.id().toString()));
      }
      assertFalse(text.contains(".db"));
      assertTrue(
          pdf.getPage(0).getMediaBox().getHeight() > pdf.getPage(0).getMediaBox().getWidth());
      for (var name : pdf.getPage(0).getResources().getFontNames()) {
        assertTrue(pdf.getPage(0).getResources().getFont(name).isEmbedded());
      }
      var resources = pdf.getPage(0).getResources();
      int photos = 0;
      for (var name : resources.getXObjectNames()) {
        if (resources.getXObject(name) instanceof PDImageXObject image) {
          photos++;
          assertEquals(320, image.getWidth());
          assertEquals(180, image.getHeight());
          assertEquals(0x457c8b, image.getImage().getRGB(50, 50) & 0xffffff);
        }
      }
      assertEquals(1, photos);
      var rendered = new PDFRenderer(pdf).renderImage(0);
      int minX = rendered.getWidth(), maxX = 0, minY = rendered.getHeight(), maxY = 0;
      for (int y = 0; y < rendered.getHeight(); y++) {
        for (int x = 0; x < rendered.getWidth(); x++) {
          if ((rendered.getRGB(x, y) & 0xffffff) == 0x457c8b) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
          }
        }
      }
      assertEquals(320.0 / 180, (maxX - minX + 1.0) / (maxY - minY + 1), 0.03);
      if (status == ProjectStatus.FINISHED) {
        Path evidence = evidence();
        Files.copy(
            output,
            evidence.resolve("complete.pdf"),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        ImageIO.write(rendered, "png", evidence.resolve("complete.png").toFile());
      }
      rendered.flush();
    }
  }

  @Test
  void minimalProjectHasMeaningfulEmptyChecklist() throws Exception {
    Path output = directory.resolve("minimal.pdf");
    writer.write(new ProjectDraft("Umhang", "", "", "", "", null), output);
    try (var pdf = Loader.loadPDF(output.toFile())) {
      String text = new BoundsStripper().getText(pdf);
      assertTrue(text.contains("Umhang"));
      assertTrue(text.contains("Status: Geplant"));
      assertTrue(text.contains("Keine Arbeitsschritte hinterlegt."));
      assertFalse(text.contains("Material:"));
      assertFalse(text.contains("Notiz"));
      assertEquals(1, pdf.getNumberOfPages());
      for (var ignored : pdf.getPage(0).getResources().getXObjectNames()) {
        fail("Unexpected image");
      }
    }
  }

  @Test
  void paginatesCompleteNotesLongWordsAndManyStepsInOrder() throws Exception {
    String note =
        IntStream.range(0, 180)
                .mapToObj(i -> "Notizzeile%03d Äußere Nähte sorgfältig prüfen.".formatted(i))
                .collect(java.util.stream.Collectors.joining("\n\n"))
            + "\n"
            + "Überlänge".repeat(180)
            + "\nNOTIZENDE";
    var steps =
        IntStream.range(0, 160)
            .mapToObj(
                i ->
                    step(
                        "Schritt%03d ".formatted(i)
                            + (i == 79 ? "Kostümstoff".repeat(120) : "Kante kontrollieren"),
                        i % 2 == 0,
                        i * 2))
            .toList();
    Path output = directory.resolve("long.pdf");
    writer.write(
        new ProjectDraft(
            "Mehrseitiger Kostümplan " + "Titelwort".repeat(35),
            "",
            "",
            "",
            note,
            null,
            ProjectStatus.STARTED,
            steps),
        output);
    try (var pdf = Loader.loadPDF(output.toFile())) {
      assertTrue(pdf.getNumberOfPages() > 10);
      String text = new BoundsStripper().getText(pdf);
      int previous = -1;
      for (int i = 0; i < 180; i++) {
        int index = text.indexOf("Notizzeile%03d".formatted(i));
        assertTrue(index > previous);
        previous = index;
      }
      assertTrue(text.indexOf("NOTIZENDE") > previous);
      previous = text.indexOf("NOTIZENDE");
      for (int i = 0; i < 160; i++) {
        int index = text.indexOf("Schritt%03d".formatted(i));
        assertTrue(index > previous);
        previous = index;
      }
      String withoutFurniture =
          text.replaceAll("Costume Planner · Projektübersicht|Seite \\d+|\\s", "");
      assertTrue(withoutFurniture.contains("Überlänge".repeat(180)));
      assertTrue(withoutFurniture.contains("Kostümstoff".repeat(120)));
      Path evidence = evidence();
      Files.copy(
          output,
          evidence.resolve("multipage.pdf"),
          java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      var renderer = new PDFRenderer(pdf);
      // Render every page. Keep representative pages for manual visual review.
      for (int page = 0; page < pdf.getNumberOfPages(); page++) {
        var rendered = renderer.renderImage(page);
        if (page == 0
            || page == 1
            || page == pdf.getNumberOfPages() - 3
            || page == pdf.getNumberOfPages() - 1) {
          ImageIO.write(
              rendered, "png", evidence.resolve("multipage-%02d.png".formatted(page + 1)).toFile());
        }
        rendered.flush();
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"漢", "\uD83E\uDDF5", "\u0000", "\u200B"})
  void unsupportedCharactersFailExplicitlyInsteadOfDisappearing(String unsupported) {
    var saved = new ProjectDraft("Kostüm", "", "", "", "Vorher " + unsupported + " Nachher", null);
    var error =
        assertThrows(
            ExportException.class, () -> writer.write(saved, directory.resolve("unknown.pdf")));
    assertTrue(error.getMessage().contains("U+"));
  }

  @Test
  void preservesExplicitBlankLinesAndDifferentLineEndings() throws Exception {
    Path output = directory.resolve("lines.pdf");
    writer.write(
        new ProjectDraft(
            "Zeilen", "", "", "", "Erste\r\n\r\nZweite\rDritte\u2028Vierte\tEnde", null),
        output);
    try (var pdf = Loader.loadPDF(output.toFile())) {
      var positions = new java.util.HashMap<String, Float>();
      var stripper =
          new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> characters)
                throws java.io.IOException {
              positions.put(text, characters.getFirst().getYDirAdj());
              super.writeString(text, characters);
            }
          };
      stripper.getText(pdf);
      assertEquals(34, positions.get("Zweite") - positions.get("Erste"), 0.1);
      assertEquals(17, positions.get("Dritte") - positions.get("Zweite"), 0.1);
    }
  }

  private static ChecklistStep step(String text, boolean completed, int position) {
    return new ChecklistStep(UUID.randomUUID(), text, completed, position);
  }

  private static Path evidence() throws Exception {
    return Files.createDirectories(Path.of("target/verification/pdf"));
  }

  private static final class BoundsStripper extends PDFTextStripper {
    @Override
    protected void processTextPosition(TextPosition position) {
      assertTrue(position.getXDirAdj() >= 47.9, position.getUnicode());
      assertTrue(
          position.getXDirAdj() + position.getWidthDirAdj() <= PDRectangle.A4.getWidth() - 47.5,
          "Text exceeds right margin: " + position.getUnicode());
      assertTrue(
          position.getYDirAdj() >= 20 && position.getYDirAdj() <= PDRectangle.A4.getHeight() - 20);
      super.processTextPosition(position);
    }
  }
}
