package de.appfabrik.kostuemplan.pdf;

import de.appfabrik.kostuemplan.application.ExportException;
import de.appfabrik.kostuemplan.application.PdfWriter;
import de.appfabrik.kostuemplan.domain.Photo;
import de.appfabrik.kostuemplan.domain.ProjectDraft;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

/** Fixed A4 layout. Each call owns its document, embedded font and layout state. */
public final class PdfBoxProjectWriter implements PdfWriter {
  @Override
  public void write(ProjectDraft saved, Path temporary) throws IOException {
    try (var document = new PDDocument();
        var source =
            PdfBoxProjectWriter.class.getResourceAsStream(
                "/de/appfabrik/kostuemplan/fonts/DejaVuSans.ttf")) {
      if (source == null) {
        throw new IOException("Bundled font missing");
      }
      var font = PDType0Font.load(document, source, true);
      document.getDocumentInformation().setTitle(saved.name());
      document.getDocumentInformation().setCreator("KostümPlan");
      try (var layout = new Layout(document, font)) {
        layout.text(saved.name(), 22, 30);
        layout.gap(8);
        layout.text("Status: " + saved.status().displayName(), 11, 17);
        layout.field("Material", saved.material());
        layout.field("Farbe", saved.color());
        layout.field("Aufbewahrungsort", saved.location());
        if (saved.photo() != null) {
          layout.photo(saved.photo());
        }
        if (!saved.note().isEmpty()) {
          layout.heading("Notiz");
          layout.text(saved.note(), 11, 17);
        }
        layout.heading("Arbeitsschritte");
        if (saved.steps().isEmpty()) {
          layout.text("Keine Arbeitsschritte hinterlegt.", 11, 17);
        }
        for (var step : saved.steps()) {
          layout.text((step.completed() ? "☑ Erledigt: " : "☐ Offen: ") + step.text(), 11, 17);
          layout.gap(6);
        }
      }
      document.save(temporary.toFile());
    }
  }

  private static final class Layout implements AutoCloseable {
    private static final float MARGIN = 48;
    private static final float WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;
    private final PDDocument document;
    private final PDType0Font font;
    private final Map<Integer, Float> widths = new HashMap<>();
    private PDPageContentStream stream;
    private float y;

    private Layout(PDDocument document, PDType0Font font) throws IOException {
      this.document = document;
      this.font = font;
      newPage();
    }

    private void newPage() throws IOException {
      close();
      var page = new PDPage(PDRectangle.A4);
      document.addPage(page);
      stream = new PDPageContentStream(document, page);
      draw("KostümPlan · Projektübersicht", 9, PDRectangle.A4.getHeight() - 30);
      draw("Seite " + document.getNumberOfPages(), 9, 28);
      y = PDRectangle.A4.getHeight() - 65;
    }

    private void ensureSpace(float height) throws IOException {
      if (y - height < MARGIN) {
        newPage();
      }
    }

    private void draw(String line, float size, float baseline) throws IOException {
      stream.beginText();
      stream.setFont(font, size);
      stream.newLineAtOffset(MARGIN, baseline);
      stream.showText(line);
      stream.endText();
    }

    private void line(String text, float size, float leading) throws IOException {
      ensureSpace(leading);
      draw(text, size, y - size);
      y -= leading;
    }

    private void gap(float points) {
      y -= points;
    }

    private void heading(String title) throws IOException {
      ensureSpace(53);
      gap(14);
      text(title, 14, 22);
    }

    private void field(String title, String value) throws IOException {
      if (!value.isEmpty()) {
        text(title + ": " + value, 11, 17);
      }
    }

    private float width(int point) throws IOException {
      Float cached = widths.get(point);
      if (cached != null) {
        return cached;
      }
      try {
        if (Character.isISOControl(point) || Character.getType(point) == Character.FORMAT) {
          throw new IllegalArgumentException("Invisible control character");
        }
        String character = new String(Character.toChars(point));
        font.encode(character);
        float value = font.getStringWidth(character) / 1000;
        widths.put(point, value);
        return value;
      } catch (IllegalArgumentException exception) {
        throw new ExportException(
            "Das Zeichen U+"
                + String.format(java.util.Locale.ROOT, "%04X", point)
                + " kann in der PDF nicht zuverlässig dargestellt werden. Bitte ersetze es im Projekttext und speichere das Projekt erneut. Es wurde keine PDF übernommen.",
            exception);
      }
    }

    private void text(String raw, float size, float leading) throws IOException {
      String text =
          Normalizer.normalize(raw, Normalizer.Form.NFC)
              .replace("\r\n", "\n")
              .replace('\r', '\n')
              .replace('\u2028', '\n')
              .replace('\u2029', '\n')
              .replace("\t", "    ");
      for (String paragraph : text.split("\n", -1)) {
        int[] points = paragraph.codePoints().toArray();
        if (points.length == 0) {
          line("", size, leading);
          continue;
        }
        int start = 0;
        while (start < points.length) {
          float measured = 0;
          int end = start;
          int lastSpace = -1;
          while (end < points.length) {
            float next = width(points[end]) * size;
            if (measured + next > WIDTH && end > start) {
              break;
            }
            measured += next;
            if (Character.isWhitespace(points[end])) {
              lastSpace = end;
            }
            end++;
          }
          // Prefer word boundaries, but split oversized words by Unicode code point.
          if (end < points.length && lastSpace > start) {
            end = lastSpace + 1;
          }
          line(new String(points, start, end - start), size, leading);
          start = end;
        }
      }
    }

    private void photo(Photo photo) throws IOException {
      // Re-encode pixels. Camera metadata and source-file information are not copied to the PDF.
      var pixels = ImageIO.read(new ByteArrayInputStream(photo.bytes()));
      if (pixels == null) {
        throw new IOException("Photo decoder unavailable");
      }
      try {
        var image = LosslessFactory.createFromImage(document, pixels);
        float scale = Math.min(WIDTH / pixels.getWidth(), 225f / pixels.getHeight());
        float width = pixels.getWidth() * scale;
        float height = pixels.getHeight() * scale;
        ensureSpace(height + 24);
        gap(12);
        stream.drawImage(image, MARGIN + (WIDTH - width) / 2, y - height, width, height);
        y -= height + 12;
      } finally {
        pixels.flush();
      }
    }

    @Override
    public void close() throws IOException {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }
  }
}
