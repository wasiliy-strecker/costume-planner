package de.appfabrik.kostuemplan.pdf;

import de.appfabrik.kostuemplan.application.ExportException;
import de.appfabrik.kostuemplan.domain.Photo;
import de.appfabrik.kostuemplan.domain.ProjectDraft;
import de.appfabrik.kostuemplan.domain.ProjectStatus;
import de.appfabrik.kostuemplan.i18n.Messages;
import java.awt.Color;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

/** App-colored A4 cards with flowing text and proportional, uncropped photographs. */
final class ProjectPdfLayout implements AutoCloseable {
  private static final float MARGIN = 48;
  private static final float WIDTH = PDRectangle.A4.getWidth() - MARGIN * 2;
  private static final float TOP = PDRectangle.A4.getHeight() - 80;
  private static final float BOTTOM = 52;
  private static final Color INK = new Color(0x302E35);
  private static final Color MUTED = new Color(0x69626A);
  private static final Color ACCENT = new Color(0x74536F);
  private static final Color SOFT = new Color(0xF1EAF0);
  private static final Color PAPER = new Color(0xF8F5F1);
  private static final Color BORDER = new Color(0xDDD4DB);
  private static final Color GREEN = new Color(0x356A52);
  private static final Color GREEN_SOFT = new Color(0xEDF5EF);
  private static final Color AMBER = new Color(0x906018);
  private static final Color AMBER_SOFT = new Color(0xFBF2DF);
  private final PDDocument document;
  private final PDType0Font regular;
  private final PDType0Font bold;
  private final Messages messages;
  private final Map<Long, Float> widths = new HashMap<>();
  private PDPageContentStream stream;
  private float y;
  private int projectCount;
  private String firstTitle;

  ProjectPdfLayout(PDDocument document, PDType0Font regular, PDType0Font bold, Messages messages) {
    this.document = document;
    this.regular = regular;
    this.bold = bold;
    this.messages = messages;
  }

  int projectCount() {
    return projectCount;
  }

  String firstTitle() {
    return firstTitle;
  }

  void project(ProjectDraft project) throws IOException {
    if (projectCount++ == 0) {
      firstTitle = project.name();
    }
    newPage();
    text(project.name(), 24, 31, true, ACCENT);
    y -= 9;
    status(project.status());
    y -= 16;
    if (!overview(project)) {
      if (project.photo() != null) {
        photo(project.photo());
      }
      field(messages.text("pdf.material"), project.material(), null);
      field(messages.text("pdf.color"), project.color(), parseColor(project.color()));
      field(messages.text("pdf.location"), project.location(), null);
    }
    if (!project.note().isEmpty()) {
      heading(messages.text("pdf.note"), 58);
      card(project.note(), PAPER, INK);
    }
    heading(messages.text("pdf.steps"), 80);
    if (project.steps().isEmpty()) {
      card(messages.text("pdf.noSteps"), SOFT, MUTED);
    } else {
      long completed = project.steps().stream().filter(step -> step.completed()).count();
      text(
          messages.text("checklist.summary", completed, project.steps().size()),
          9,
          15,
          false,
          MUTED);
      ensure(15);
      rect(MARGIN, y - 4, WIDTH, 4, SOFT);
      if (completed > 0) {
        rect(MARGIN, y - 4, WIDTH * completed / project.steps().size(), 4, GREEN);
      }
      y -= 15;
      for (var step : project.steps()) {
        card(
            (step.completed() ? messages.text("pdf.completed") : messages.text("pdf.open"))
                + step.text(),
            step.completed() ? GREEN_SOFT : PAPER,
            step.completed() ? GREEN : INK);
        y -= 5;
      }
    }
  }

  private void newPage() throws IOException {
    close();
    var page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    stream = new PDPageContentStream(document, page);
    float headerTop = PDRectangle.A4.getHeight() - 28;
    rect(0, PDRectangle.A4.getHeight() - 8, PDRectangle.A4.getWidth(), 8, ACCENT);
    draw(messages.text("pdf.header"), MARGIN, headerTop - 12, 10, true, ACCENT);
    rect(MARGIN, headerTop - 27, WIDTH, 1, BORDER);
    y = TOP;
  }

  private void ensure(float height) throws IOException {
    if (y - height < BOTTOM) {
      newPage();
    }
  }

  private void status(ProjectStatus status) throws IOException {
    String label = messages.text("pdf.status", messages.status(status));
    Color fill =
        switch (status) {
          case PLANNED -> SOFT;
          case STARTED -> AMBER_SOFT;
          case FINISHED -> GREEN_SOFT;
        };
    Color color =
        switch (status) {
          case PLANNED -> ACCENT;
          case STARTED -> AMBER;
          case FINISHED -> GREEN;
        };
    ensure(30);
    float width = Math.min(WIDTH, measure(label, 10, true) + 26);
    box(MARGIN, y, width, 28, fill);
    draw(label, MARGIN + 13, y - 18, 10, true, color);
    y -= 28;
  }

  private void heading(String title, float minimum) throws IOException {
    ensure(minimum);
    y -= 12;
    draw(title, MARGIN, y - 14, 14, true, ACCENT);
    y -= 26;
  }

  private void field(String label, String value, Color swatch) throws IOException {
    if (value.isEmpty()) {
      return;
    }
    float labelWidth = 138;
    float valueX = MARGIN + labelWidth + 14 + (swatch == null ? 0 : 36);
    var labels = wrap(label, labelWidth - 24, 9, true);
    var lines = wrap(value, MARGIN + WIDTH - valueX - 14, 11, false);
    int offset = 0;
    while (offset < Math.max(labels.size(), lines.size())) {
      ensure(45);
      int capacity = Math.max(1, (int) ((y - BOTTOM - 24) / 17));
      int count = Math.min(capacity, Math.max(labels.size(), lines.size()) - offset);
      float height = Math.max(42, count * 17 + 24);
      box(MARGIN, y, WIDTH, height, PAPER);
      rect(MARGIN, y - height, 3, height, SOFT);
      for (int i = 0; i < count; i++) {
        float baseline = y - 24 - i * 17;
        if (offset + i < labels.size()) {
          draw(labels.get(offset + i), MARGIN + 14, baseline, 9, true, MUTED);
        }
        if (offset + i < lines.size()) {
          draw(lines.get(offset + i), valueX, baseline, 11, false, INK);
        }
      }
      if (swatch != null && offset == 0) {
        swatch(swatch, MARGIN + labelWidth + 14, y - 31);
      }
      y -= height + 6;
      offset += count;
    }
  }

  private void card(String raw, Color fill, Color textColor) throws IOException {
    var lines = wrap(raw, WIDTH - 28, 11, false);
    int offset = 0;
    while (offset < lines.size()) {
      ensure(41);
      int count = Math.min(lines.size() - offset, Math.max(1, (int) ((y - BOTTOM - 24) / 17)));
      float height = count * 17 + 24;
      box(MARGIN, y, WIDTH, height, fill);
      for (int i = 0; i < count; i++) {
        draw(lines.get(offset + i), MARGIN + 14, y - 24 - i * 17, 11, false, textColor);
      }
      y -= height;
      offset += count;
    }
  }

  private void text(String raw, float size, float leading, boolean strong, Color color)
      throws IOException {
    for (String line : wrap(raw, WIDTH, size, strong)) {
      ensure(leading);
      draw(line, MARGIN, y - size, size, strong, color);
      y -= leading;
    }
  }

  private void photo(Photo photo) throws IOException {
    try (var input = photo.openStream()) {
      var pixels = ImageIO.read(input);
      if (pixels == null) {
        throw new IOException("Photo decoder unavailable");
      }
      try {
        // Decode and re-encode pixels so source metadata is never copied to the PDF.
        var image = LosslessFactory.createFromImage(document, pixels);
        float scale = Math.min((WIDTH - 20) / pixels.getWidth(), 175f / pixels.getHeight());
        float width = pixels.getWidth() * scale;
        float height = pixels.getHeight() * scale;
        ensure(height + 32);
        box(MARGIN, y, WIDTH, height + 20, PAPER);
        stream.drawImage(image, MARGIN + (WIDTH - width) / 2, y - height - 10, width, height);
        y -= height + 32;
      } finally {
        pixels.flush();
      }
    }
  }

  private record Detail(String label, List<String> lines, Color color) {}

  private boolean overview(ProjectDraft project) throws IOException {
    if (project.photo() == null) {
      return false;
    }
    float half = (WIDTH - 14) / 2;
    List<Detail> details = new ArrayList<>();
    String[][] values = {
      {messages.text("pdf.material"), project.material()},
      {messages.text("pdf.color"), project.color()},
      {messages.text("pdf.location"), project.location()}
    };
    float required = 16;
    for (int i = 0; i < values.length; i++) {
      if (values[i][1].isEmpty()) {
        continue;
      }
      Color swatch = i == 1 ? parseColor(values[i][1]) : null;
      var lines = wrap(values[i][1], half - 28 - (swatch == null ? 0 : 36), 11, false);
      if (measure(values[i][0], 9, true) > half - 28) {
        return false;
      }
      details.add(new Detail(values[i][0], lines, swatch));
      required += 24 + lines.size() * 17 + 10;
    }
    if (details.isEmpty() || required > 235) {
      return false;
    }
    float height = Math.max(195, required);
    ensure(height + 12);
    box(MARGIN, y, half, height, PAPER);
    float top = y - 16;
    for (Detail detail : details) {
      draw(detail.label(), MARGIN + 14, top - 9, 9, true, MUTED);
      float x = MARGIN + 14 + (detail.color() == null ? 0 : 36);
      for (int i = 0; i < detail.lines().size(); i++) {
        draw(detail.lines().get(i), x, top - 33 - i * 17, 11, false, INK);
      }
      if (detail.color() != null) {
        swatch(detail.color(), MARGIN + 14, top - 39);
      }
      top -= 34 + detail.lines().size() * 17;
    }
    float photoX = MARGIN + half + 14;
    box(photoX, y, half, height, PAPER);
    try (var input = project.photo().openStream()) {
      var pixels = ImageIO.read(input);
      if (pixels == null) {
        throw new IOException("Photo decoder unavailable");
      }
      try {
        var image = LosslessFactory.createFromImage(document, pixels);
        float scale = Math.min((half - 16) / pixels.getWidth(), (height - 16) / pixels.getHeight());
        float width = pixels.getWidth() * scale;
        float imageHeight = pixels.getHeight() * scale;
        stream.drawImage(
            image, photoX + (half - width) / 2, y - (height + imageHeight) / 2, width, imageHeight);
      } finally {
        pixels.flush();
      }
    }
    y -= height + 12;
    return true;
  }

  private static Color parseColor(String value) {
    if (!value.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
      return null;
    }
    int rgb = Integer.parseInt(value.substring(1, 7), 16);
    int alpha = value.length() == 9 ? Integer.parseInt(value.substring(7), 16) : 255;
    return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha);
  }

  private void swatch(Color color, float x, float bottom) throws IOException {
    // A checkerboard makes transparent selected colors visible without inventing an opaque color.
    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 4; column++) {
        int base = (row + column) % 2 == 0 ? 255 : 220;
        float a = color.getAlpha() / 255f;
        Color blended =
            new Color(
                Math.round(color.getRed() * a + base * (1 - a)),
                Math.round(color.getGreen() * a + base * (1 - a)),
                Math.round(color.getBlue() * a + base * (1 - a)));
        rect(x + column * 7, bottom + row * 7, 7, 7, blended);
      }
    }
    stream.setStrokingColor(MUTED);
    stream.setLineWidth(0.5f);
    stream.addRect(x, bottom, 28, 21);
    stream.stroke();
  }

  private float width(int point, boolean strong) throws IOException {
    long key = ((long) point << 1) | (strong ? 1 : 0);
    Float cached = widths.get(key);
    if (cached != null) {
      return cached;
    }
    try {
      if (Character.isISOControl(point) || Character.getType(point) == Character.FORMAT) {
        throw new IllegalArgumentException("Invisible control character");
      }
      String character = new String(Character.toChars(point));
      var font = strong ? bold : regular;
      font.encode(character);
      float value = font.getStringWidth(character) / 1000;
      widths.put(key, value);
      return value;
    } catch (IllegalArgumentException exception) {
      throw new ExportException(
          ExportException.Code.UNSUPPORTED_GLYPH,
          "U+" + String.format(Locale.ROOT, "%04X", point),
          exception);
    }
  }

  private float measure(String text, float size, boolean strong) throws IOException {
    float result = 0;
    for (int point : text.codePoints().toArray()) {
      result += width(point, strong) * size;
    }
    return result;
  }

  private List<String> wrap(String raw, float available, float size, boolean strong)
      throws IOException {
    String normalized =
        Normalizer.normalize(raw, Normalizer.Form.NFC)
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace('\u2028', '\n')
            .replace('\u2029', '\n')
            .replace("\t", "    ");
    List<String> lines = new ArrayList<>();
    for (String paragraph : normalized.split("\n", -1)) {
      int[] points = paragraph.codePoints().toArray();
      if (points.length == 0) {
        lines.add("");
      }
      int start = 0;
      while (start < points.length) {
        float measured = 0;
        int end = start;
        int lastSpace = -1;
        while (end < points.length) {
          float next = width(points[end], strong) * size;
          if (measured + next > available && end > start) {
            break;
          }
          measured += next;
          if (Character.isWhitespace(points[end])) {
            lastSpace = end;
          }
          end++;
        }
        if (end < points.length && lastSpace > start) {
          end = lastSpace + 1;
        }
        lines.add(new String(points, start, end - start));
        start = end;
      }
    }
    return lines;
  }

  private void draw(String text, float x, float baseline, float size, boolean strong, Color color)
      throws IOException {
    // Validate static labels too, through the same explicit unsupported-glyph path.
    measure(text, size, strong);
    stream.setNonStrokingColor(color);
    stream.beginText();
    stream.setFont(strong ? bold : regular, size);
    stream.newLineAtOffset(x, baseline);
    stream.showText(text);
    stream.endText();
  }

  private void rect(float x, float bottom, float width, float height, Color fill)
      throws IOException {
    stream.setNonStrokingColor(fill);
    stream.addRect(x, bottom, width, height);
    stream.fill();
  }

  private void box(float x, float top, float width, float height, Color fill) throws IOException {
    float bottom = top - height;
    float r = 6;
    float c = r * 0.55228475f;
    stream.setNonStrokingColor(fill);
    stream.moveTo(x + r, bottom);
    stream.lineTo(x + width - r, bottom);
    stream.curveTo(x + width - r + c, bottom, x + width, bottom + r - c, x + width, bottom + r);
    stream.lineTo(x + width, top - r);
    stream.curveTo(x + width, top - r + c, x + width - r + c, top, x + width - r, top);
    stream.lineTo(x + r, top);
    stream.curveTo(x + r - c, top, x, top - r + c, x, top - r);
    stream.lineTo(x, bottom + r);
    stream.curveTo(x, bottom + r - c, x + r - c, bottom, x + r, bottom);
    stream.closePath();
    stream.fill();
  }

  void finish() throws IOException {
    close();
    int total = document.getNumberOfPages();
    for (int index = 0; index < total; index++) {
      stream =
          new PDPageContentStream(
              document, document.getPage(index), PDPageContentStream.AppendMode.APPEND, true, true);
      rect(MARGIN, 38, WIDTH, 1, BORDER);
      draw("Costume Planner", MARGIN, 24, 8, true, ACCENT);
      String page = messages.text("pdf.pageOf", index + 1, total);
      draw(page, MARGIN + WIDTH - measure(page, 8, false), 24, 8, false, MUTED);
      close();
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
